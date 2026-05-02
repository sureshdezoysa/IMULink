package com.daqmobile.imulink.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImuRepository(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val magSensor   = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _latestSample = MutableStateFlow(ImuSample())
    val latestSample: StateFlow<ImuSample> = _latestSample.asStateFlow()

    private var accelCache = AxisData()
    private var gyroCache  = AxisData()
    private var magCache   = AxisData()

    val sensorInfos: Map<String, SensorInfo> = buildSensorInfos()

    private fun buildSensorInfos(): Map<String, SensorInfo> {
        fun infoFor(sensor: Sensor?): SensorInfo {
            return if (sensor != null) {
                val maxHz = if (sensor.minDelay > 0) (1_000_000 / sensor.minDelay) else 0
                SensorInfo(
                    name            = sensor.name,
                    vendor          = sensor.vendor,
                    version         = sensor.version,
                    maxRangeRaw     = sensor.maximumRange,
                    resolutionRaw   = sensor.resolution,
                    maxSampleRateHz = maxHz,
                    available       = true
                )
            } else {
                SensorInfo("Not available", "", 0, 0f, 0f, 0, false)
            }
        }
        return mapOf(
            "Accelerometer" to infoFor(accelSensor),
            "Gyroscope"     to infoFor(gyroSensor),
            "Magnetometer"  to infoFor(magSensor)
        )
    }

    private var activeListener: SensorEventListener? = null

    fun start(sampleRateUs: Int = 20_000) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val v = event.values
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER  -> accelCache = AxisData(v[0], v[1], v[2])
                    Sensor.TYPE_GYROSCOPE      -> gyroCache  = AxisData(v[0], v[1], v[2])
                    Sensor.TYPE_MAGNETIC_FIELD -> magCache   = AxisData(v[0], v[1], v[2])
                }
                _latestSample.value = ImuSample(
                    timestampMs   = System.currentTimeMillis(),
                    accelerometer = accelCache,
                    gyroscope     = gyroCache,
                    magnetometer  = magCache
                )
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        activeListener = listener
        accelSensor?.let { sensorManager.registerListener(listener, it, sampleRateUs) }
        gyroSensor?.let  { sensorManager.registerListener(listener, it, sampleRateUs) }
        magSensor?.let   { sensorManager.registerListener(listener, it, sampleRateUs) }
    }

    fun stop() {
        activeListener?.let { sensorManager.unregisterListener(it) }
        activeListener = null
    }
}
