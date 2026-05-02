package com.daqmobile.imulink.sensor

data class AxisData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

data class ImuSample(
    val timestampMs: Long = 0L,
    val accelerometer: AxisData = AxisData(),
    val gyroscope: AxisData = AxisData(),
    val magnetometer: AxisData = AxisData()
) {
    fun toCsv(): String = buildString {
        append(timestampMs); append(',')
        append(accelerometer.x); append(',')
        append(accelerometer.y); append(',')
        append(accelerometer.z); append(',')
        append(gyroscope.x);     append(',')
        append(gyroscope.y);     append(',')
        append(gyroscope.z);     append(',')
        append(magnetometer.x);  append(',')
        append(magnetometer.y);  append(',')
        append(magnetometer.z);  append('\n')
    }
}

data class SensorInfo(
    val name: String,
    val vendor: String,
    val version: Int,
    val maxRangeRaw: Float,
    val resolutionRaw: Float,
    val maxSampleRateHz: Int,
    val available: Boolean
)
