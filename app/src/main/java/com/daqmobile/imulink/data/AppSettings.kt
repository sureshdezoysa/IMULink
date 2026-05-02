package com.daqmobile.imulink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "imulink_settings")

data class AppSettings(
    val receiverIp: String  = "192.168.1.100",
    val udpPort: Int        = 5005,
    val sampleRateHz: Int   = 50,
    val runTimeSecs: Int    = 300,
    val isPro: Boolean      = false
)

object SettingsKeys {
    val RECEIVER_IP    = stringPreferencesKey("receiver_ip")
    val UDP_PORT       = intPreferencesKey("udp_port")
    val SAMPLE_RATE_HZ = intPreferencesKey("sample_rate_hz")
    val RUN_TIME_SECS  = intPreferencesKey("run_time_secs")
    val IS_PRO         = booleanPreferencesKey("is_pro")
}

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            receiverIp   = prefs[SettingsKeys.RECEIVER_IP]    ?: "192.168.1.100",
            udpPort      = prefs[SettingsKeys.UDP_PORT]       ?: 5005,
            sampleRateHz = prefs[SettingsKeys.SAMPLE_RATE_HZ] ?: 50,
            runTimeSecs  = prefs[SettingsKeys.RUN_TIME_SECS]  ?: 300,
            isPro        = prefs[SettingsKeys.IS_PRO]         ?: false
        )
    }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.RECEIVER_IP]    = settings.receiverIp
            prefs[SettingsKeys.UDP_PORT]       = settings.udpPort
            prefs[SettingsKeys.SAMPLE_RATE_HZ] = settings.sampleRateHz
            prefs[SettingsKeys.RUN_TIME_SECS]  = settings.runTimeSecs
            prefs[SettingsKeys.IS_PRO]         = settings.isPro
        }
    }
}
