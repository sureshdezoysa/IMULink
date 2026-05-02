package com.daqmobile.imulink.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.SettingsRepository
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

enum class StreamState { IDLE, STREAMING, COUNTDOWN }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val imuRepository      = ImuRepository(app)
    val settingsRepository = SettingsRepository(app)
    private val udpSender  = UdpSender()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _displaySample = MutableStateFlow(ImuSample())
    val displaySample: StateFlow<ImuSample> = _displaySample.asStateFlow()

    private val _streamState = MutableStateFlow(StreamState.IDLE)
    val streamState: StateFlow<StreamState> = _streamState.asStateFlow()

    private val _countdownSecs = MutableStateFlow(0)
    val countdownSecs: StateFlow<Int> = _countdownSecs.asStateFlow()

    private val _deviceIp = MutableStateFlow(getDeviceIp())
    val deviceIp: StateFlow<String> = _deviceIp.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // true = red warning, false = normal grey text
    private val _statusIsError = MutableStateFlow(false)
    val statusIsError: StateFlow<Boolean> = _statusIsError.asStateFlow()

    private var displayJob:   Job? = null
    private var countdownJob: Job? = null
    private var udpJob:       Job? = null

    init {
        imuRepository.start(sampleRateUs = 20_000)
        startDisplayRefresh()
    }

    private fun startDisplayRefresh() {
        displayJob?.cancel()
        displayJob = viewModelScope.launch {
            while (true) {
                _displaySample.value = imuRepository.latestSample.value
                // Also refresh device IP in case Wi-Fi connected after app opened
                _deviceIp.value = getDeviceIp()
                delay(100L)
            }
        }
    }

    fun startStreaming() {
        val cfg = settings.value

        // ── Network check ──────────────────────────────────────────────────
        val networkType = getNetworkType()
        if (networkType == NetworkType.NONE) {
            _statusMessage.value = "No network — connect to Wi-Fi or hotspot first"
            _statusIsError.value = true
            return
        }

        viewModelScope.launch {
            // Open UDP socket
            val opened = udpSender.open(cfg.receiverIp, cfg.udpPort)
            if (!opened) {
                _statusMessage.value = "Invalid IP address: ${cfg.receiverIp}"
                _statusIsError.value = true
                return@launch
            }

            val networkLabel = when (networkType) {
                NetworkType.WIFI    -> "Wi-Fi"
                NetworkType.HOTSPOT -> "Hotspot"
                else                -> "Network"
            }
            _statusMessage.value = "Streaming via $networkLabel → ${cfg.receiverIp}:${cfg.udpPort}"
            _statusIsError.value = false

            // Set stream state
            if (!cfg.isPro) {
                _countdownSecs.value = cfg.runTimeSecs
                _streamState.value   = StreamState.COUNTDOWN
                countdownJob = viewModelScope.launch {
                    while (_countdownSecs.value > 0) {
                        delay(1_000L)
                        _countdownSecs.value -= 1
                    }
                    stopStreaming()
                }
            } else {
                _streamState.value = StreamState.STREAMING
            }

            // UDP send loop
            udpJob = viewModelScope.launch {
                while (true) {
                    val csv = imuRepository.latestSample.value.toCsv()
                    udpSender.send(csv)
                    val delayMs = (1000f / cfg.sampleRateHz).toLong()
                    delay(delayMs)
                }
            }
        }
    }

    fun stopStreaming() {
        countdownJob?.cancel(); countdownJob = null
        udpJob?.cancel();       udpJob       = null
        udpSender.close()
        _streamState.value   = StreamState.IDLE
        _countdownSecs.value = 0
        _statusMessage.value = ""
        _statusIsError.value = false
    }

    fun toggleStreaming() {
        if (_streamState.value == StreamState.IDLE) startStreaming()
        else stopStreaming()
    }

    override fun onCleared() {
        super.onCleared()
        displayJob?.cancel()
        countdownJob?.cancel()
        udpJob?.cancel()
        udpSender.close()
        imuRepository.stop()
    }

    // ── Network helpers ───────────────────────────────────────────────────

    enum class NetworkType { NONE, WIFI, HOTSPOT, OTHER }

    private fun getNetworkType(): NetworkType {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return NetworkType.NONE
        val caps    = cm.getNetworkCapabilities(network) ?: return NetworkType.NONE
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.HOTSPOT
            else -> NetworkType.OTHER
        }
    }

    private fun getDeviceIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "—"
            for (iface in interfaces.asSequence()) {
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
