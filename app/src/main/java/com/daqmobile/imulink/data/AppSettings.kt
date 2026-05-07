package com.daqmobile.imulink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "imulink_settings")

data class AppSettings(
    val receiverIp:          String  = "",
    val udpPort:             Int     = 5005,
    val sampleRateHz:        Int     = 50,
    // All sensors enabled by default — no Pro gating
    val enableAccelerometer: Boolean = true,
    val enableGyroscope:     Boolean = true,
    val enableMagnetometer:  Boolean = true,
    val enableGravity:       Boolean = true,
    val enableLinearAccel:   Boolean = true,
    val enableRotation:      Boolean = true
)

object SettingsKeys {
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val RECEIVER_IP           = stringPreferencesKey("receiver_ip")
    val UDP_PORT              = intPreferencesKey("udp_port")
    val SAMPLE_RATE_HZ        = intPreferencesKey("sample_rate_hz")
    val ENABLE_ACCELEROMETER  = booleanPreferencesKey("enable_accelerometer")
    val ENABLE_GYROSCOPE      = booleanPreferencesKey("enable_gyroscope")
    val ENABLE_MAGNETOMETER   = booleanPreferencesKey("enable_magnetometer")
    val ENABLE_GRAVITY        = booleanPreferencesKey("enable_gravity")
    val ENABLE_LINEAR_ACCEL   = booleanPreferencesKey("enable_linear_accel")
    val ENABLE_ROTATION       = booleanPreferencesKey("enable_rotation")
}

object Validator {
    fun isValidIp(ip: String): Boolean {
        if (ip.isBlank()) return false
        val parts  = ip.trim().split(".")
        if (parts.size != 4) return false
        val octets = parts.mapNotNull { it.toIntOrNull() }
        if (octets.size != 4) return false
        if (octets.any { it < 0 || it > 255 }) return false
        if (octets[0] == 0)           return false
        if (octets.all { it == 255 }) return false
        if (octets[3] == 255)         return false
        if (octets[3] == 0)           return false
        return true
    }
    fun isValidPort(port: Int): Boolean = port in 1024..65535
    fun isValidPort(s: String): Boolean = s.toIntOrNull()?.let { isValidPort(it) } ?: false
    fun isReadyToStream(ip: String, port: Int) = isValidIp(ip) && isValidPort(port)
}

fun buildSampleRateOptions(maxSensorHz: Int): List<Int> {
    val base = listOf(10, 20, 30, 40, 50)
    if (maxSensorHz <= 50) return base
    val extra = mutableListOf<Int>()
    var hz = 100
    while (hz < maxSensorHz) { extra.add(hz); hz += 100 }
    if (!extra.contains(maxSensorHz)) extra.add(maxSensorHz)
    return base + extra
}

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            receiverIp          = p[SettingsKeys.RECEIVER_IP]          ?: "",
            udpPort             = p[SettingsKeys.UDP_PORT]             ?: 5005,
            sampleRateHz        = p[SettingsKeys.SAMPLE_RATE_HZ]       ?: 50,
            enableAccelerometer = p[SettingsKeys.ENABLE_ACCELEROMETER] ?: true,
            enableGyroscope     = p[SettingsKeys.ENABLE_GYROSCOPE]     ?: true,
            enableMagnetometer  = p[SettingsKeys.ENABLE_MAGNETOMETER]  ?: true,
            enableGravity       = p[SettingsKeys.ENABLE_GRAVITY]       ?: true,
            enableLinearAccel   = p[SettingsKeys.ENABLE_LINEAR_ACCEL]  ?: true,
            enableRotation      = p[SettingsKeys.ENABLE_ROTATION]      ?: true
        )
    }

    suspend fun isOnboardingDone(): Boolean {
        return context.dataStore.data.first()[SettingsKeys.ONBOARDING_DONE] ?: false
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { p ->
            p[SettingsKeys.ONBOARDING_DONE] = true
        }
    }

    suspend fun save(s: AppSettings) {
        context.dataStore.edit { p ->
            p[SettingsKeys.RECEIVER_IP]          = s.receiverIp
            p[SettingsKeys.UDP_PORT]             = s.udpPort
            p[SettingsKeys.SAMPLE_RATE_HZ]       = s.sampleRateHz
            p[SettingsKeys.ENABLE_ACCELEROMETER] = s.enableAccelerometer
            p[SettingsKeys.ENABLE_GYROSCOPE]     = s.enableGyroscope
            p[SettingsKeys.ENABLE_MAGNETOMETER]  = s.enableMagnetometer
            p[SettingsKeys.ENABLE_GRAVITY]       = s.enableGravity
            p[SettingsKeys.ENABLE_LINEAR_ACCEL]  = s.enableLinearAccel
            p[SettingsKeys.ENABLE_ROTATION]      = s.enableRotation
        }
    }
}
