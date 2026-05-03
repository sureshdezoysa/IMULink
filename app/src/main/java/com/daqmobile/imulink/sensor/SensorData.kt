package com.daqmobile.imulink.sensor

data class AxisData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

data class RotationData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val w: Float = 0f
)

data class ImuSample(
    val timestampMs:        Long         = 0L,
    val accelerometer:      AxisData     = AxisData(),
    val gyroscope:          AxisData     = AxisData(),
    val magnetometer:       AxisData     = AxisData(),
    val gravity:            AxisData     = AxisData(),
    val linearAcceleration: AxisData     = AxisData(),
    val rotation:           RotationData = RotationData()
) {
    /**
     * Build a CSV string containing ONLY the enabled sensors.
     * Format: timestamp_ms, [ax,ay,az,] [gx,gy,gz,] [mx,my,mz,]
     *         [gravx,gravy,gravz,] [lax,lay,laz,] [rotx,roty,rotz,rotw]
     * No empty placeholders — disabled sensors are completely absent.
     */
    fun toCsv(
        accel:   Boolean = true,
        gyro:    Boolean = true,
        mag:     Boolean = true,
        gravity: Boolean = false,
        linear:  Boolean = false,
        rot:     Boolean = false
    ): String = buildString {
        append(timestampMs)
        if (accel) {
            append(','); append(accelerometer.x)
            append(','); append(accelerometer.y)
            append(','); append(accelerometer.z)
        }
        if (gyro) {
            append(','); append(gyroscope.x)
            append(','); append(gyroscope.y)
            append(','); append(gyroscope.z)
        }
        if (mag) {
            append(','); append(magnetometer.x)
            append(','); append(magnetometer.y)
            append(','); append(magnetometer.z)
        }
        if (gravity) {
            append(','); append(this@ImuSample.gravity.x)
            append(','); append(this@ImuSample.gravity.y)
            append(','); append(this@ImuSample.gravity.z)
        }
        if (linear) {
            append(','); append(linearAcceleration.x)
            append(','); append(linearAcceleration.y)
            append(','); append(linearAcceleration.z)
        }
        if (rot) {
            append(','); append(rotation.x)
            append(','); append(rotation.y)
            append(','); append(rotation.z)
            append(','); append(rotation.w)
        }
        append('\n')
    }
}

data class SensorInfo(
    val name:             String,
    val vendor:           String,
    val version:          Int,
    val maxRangeRaw:      Float,
    val resolutionRaw:    Float,
    val maxSampleRateHz:  Int,
    val available:        Boolean
)
