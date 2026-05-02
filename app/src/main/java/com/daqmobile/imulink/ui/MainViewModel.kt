package com.daqmobile.imulink.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.SettingsRepository
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

    private var displayJob:   Job? = null
    private var countdownJob: Job? = null
    private var udpJob:       Job? = null

    init {
        // Start sensors immediately when app opens
        imuRepository.start(sampleRateUs = 20_000)
        // Start display refresh immediately — readings show before pressing Start
        startDisplayRefresh()
    }

    private fun startDisplayRefresh() {
        displayJob?.cancel()
        displayJob = viewModelScope.launch {
            while (true) {
                _displaySample.value = imuRepository.latestSample.value
                delay(100L)
            }
        }
    }

    fun startStreaming() {
        val cfg = settings.value
        if (!cfg.isPro) {
            _countdownSecs.value = cfg.runTimeSecs
            _streamState.value = StreamState.COUNTDOWN
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
        udpJob = viewModelScope.launch {
            // TODO Sprint 3: UDP transmission
        }
    }

    fun stopStreaming() {
        countdownJob?.cancel(); countdownJob = null
        udpJob?.cancel();       udpJob       = null
        _streamState.value   = StreamState.IDLE
        _countdownSecs.value = 0
        // displayJob keeps running — readings stay live always
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
        imuRepository.stop()
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
