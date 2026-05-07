package com.daqmobile.imulink.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daqmobile.imulink.R
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.SettingsRepository
import com.daqmobile.imulink.data.Validator
import com.daqmobile.imulink.network.StreamingService
import com.daqmobile.imulink.network.UdpSender
import com.daqmobile.imulink.sensor.ImuRepository
import com.daqmobile.imulink.sensor.ImuSample
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

enum class StreamState { IDLE, STREAMING }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val imuRepository      = ImuRepository(app)
    val settingsRepository = SettingsRepository(app)
    private val udpSender  = UdpSender()
    private val appContext  = app.applicationContext

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _displaySample = MutableStateFlow(ImuSample())
    val displaySample: StateFlow<ImuSample> = _displaySample.asStateFlow()

    private val _streamState = MutableStateFlow(StreamState.IDLE)
    val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

    private val _deviceIp = MutableStateFlow(getDeviceIp())
    val deviceIp: StateFlow<String> = _deviceIp.asStateFlow()

    private val _popupMessage = MutableStateFlow("")
    val popupMessage: StateFlow<String> = _popupMessage.asStateFlow()

    private val _hasNetwork = MutableStateFlow(false)
    val hasNetwork: StateFlow<Boolean> = _hasNetwork.asStateFlow()

    private val _dataRateBps = MutableStateFlow(0)
    val dataRateBps: StateFlow<Int> = _dataRateBps.asStateFlow()

    private var displayJob: Job? = null
    private var udpJob:     Job? = null
    private var rateJob:    Job? = null
    private var popupJob:   Job? = null

    private var bytesSentThisSecond = 0
    private var serviceRunning      = false

    // Receives STOP broadcast from notification STOP button
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == StreamingService.ACTION_STOP) {
                stopStreaming()
            }
        }
    }

    init {
        imuRepository.start(sampleRateUs = 20_000)
        startDisplayRefresh()
        registerStopReceiver()
    }

    private fun registerStopReceiver() {
        val filter = IntentFilter(StreamingService.ACTION_STOP)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(stopReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                appContext.registerReceiver(stopReceiver, filter)
            }
        } catch (_: Exception) {}
    }

    private fun startDisplayRefresh() {
        displayJob?.cancel()
        displayJob = viewModelScope.launch {
            while (true) {
                _displaySample.value = imuRepository.latestSample.value
                _deviceIp.value      = getDeviceIp()
                _hasNetwork.value    = isNetworkAvailable()
                delay(100L)
            }
        }
    }

    fun showPopup(message: String) {
        popupJob?.cancel()
        _popupMessage.value = message
        popupJob = viewModelScope.launch {
            delay(2_000L)
            _popupMessage.value = ""
        }
    }

    fun startStreaming() {
        val cfg = settings.value

        if (!isNetworkAvailable()) {
            showPopup(appContext.getString(R.string.status_no_wifi))
            return
        }
        if (!Validator.isReadyToStream(cfg.receiverIp, cfg.udpPort)) {
            showPopup(appContext.getString(R.string.status_invalid_ip_port))
            return
        }

        viewModelScope.launch {
            val opened = udpSender.open(cfg.receiverIp, cfg.udpPort)
            if (!opened) {
                showPopup(appContext.getString(
                    R.string.status_cannot_connect, cfg.receiverIp))
                return@launch
            }

            _streamState.value = StreamState.STREAMING

            // Start foreground service
            try {
                val svcIntent = StreamingService.startIntent(
                    appContext, cfg.receiverIp, cfg.udpPort, cfg.sampleRateHz)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(svcIntent)
                } else {
                    appContext.startService(svcIntent)
                }
                serviceRunning = true
            } catch (e: Exception) {
                // Service failed to start — streaming still works, just no notification
                serviceRunning = false
            }

            // Restart sensors at configured rate
            val sampleRateUs = (1_000_000f / cfg.sampleRateHz).toInt()
            imuRepository.stop()
            imuRepository.start(sampleRateUs = sampleRateUs)

            bytesSentThisSecond = 0
            val udpDelayMs = (1000f / cfg.sampleRateHz).toLong()

            udpJob = viewModelScope.launch {
                while (true) {
                    val csv = imuRepository.latestSample.value.toCsv(
                        accel   = cfg.enableAccelerometer,
                        gyro    = cfg.enableGyroscope,
                        mag     = cfg.enableMagnetometer,
                        gravity = cfg.enableGravity,
                        linear  = cfg.enableLinearAccel,
                        rot     = cfg.enableRotation
                    )
                    udpSender.send(csv)
                    bytesSentThisSecond += csv.length
                    delay(udpDelayMs)
                }
            }

            rateJob = viewModelScope.launch {
                while (true) {
                    delay(1_000L)
                    _dataRateBps.value  = bytesSentThisSecond
                    bytesSentThisSecond = 0
                    // Update notification with live data rate
                    if (serviceRunning) {
                        try {
                            StreamingService.updateNotification(
                                appContext,
                                cfg.sampleRateHz,
                                formatDataRate(_dataRateBps.value)
                            )
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    fun stopStreaming() {
        udpJob?.cancel();  udpJob  = null
        rateJob?.cancel(); rateJob = null
        udpSender.close()
        _streamState.value  = StreamState.IDLE
        _dataRateBps.value  = 0
        bytesSentThisSecond = 0

        // Stop foreground service safely
        if (serviceRunning) {
            try {
                appContext.stopService(
                    Intent(appContext, StreamingService::class.java))
            } catch (_: Exception) {}
            serviceRunning = false
        }

        // Restart sensors at display rate
        imuRepository.stop()
        imuRepository.start(sampleRateUs = 20_000)
    }

    fun toggleStreaming() {
        if (_streamState.value == StreamState.IDLE) startStreaming()
        else stopStreaming()
    }

    override fun onCleared() {
        super.onCleared()
        displayJob?.cancel()
        udpJob?.cancel()
        rateJob?.cancel()
        popupJob?.cancel()
        udpSender.close()
        imuRepository.stop()
        try { appContext.unregisterReceiver(stopReceiver) } catch (_: Exception) {}
        if (serviceRunning) {
            try {
                appContext.stopService(
                    Intent(appContext, StreamingService::class.java))
            } catch (_: Exception) {}
        }
    }

    fun formatDataRate(bps: Int): String = when {
        bps >= 1_000_000 -> "${"%.1f".format(bps / 1_000_000f)} MB/s"
        bps >= 1_000     -> "${"%.1f".format(bps / 1_000f)} KB/s"
        else             -> "$bps B/s"
    }

    private fun isNetworkAvailable(): Boolean {
        val cm   = appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                   as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false)
                   ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     ||
               caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
               caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun getDeviceIp(): String {
        try {
            for (iface in NetworkInterface.getNetworkInterfaces()
                              ?.asSequence() ?: return "—") {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.inetAddresses.asSequence()) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address)
                        return addr.hostAddress ?: "—"
                }
            }
        } catch (_: Exception) {}
        return "—"
    }
}
