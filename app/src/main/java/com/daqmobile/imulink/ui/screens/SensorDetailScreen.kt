package com.daqmobile.imulink.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqmobile.imulink.sensor.SensorInfo
import com.daqmobile.imulink.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    sensorKey: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val info: SensorInfo? = viewModel.imuRepository.sensorInfos[sensorKey]

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        sensorKey.uppercase(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.background,
                    titleContentColor          = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            if (info == null || !info.available) {
                // Not available
                Surface(
                    shape    = MaterialTheme.shapes.medium,
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "This sensor is not available on your device.",
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                // Spec card
                DetailSection("HARDWARE SPECIFICATIONS") {
                    DetailRow("Model",       info.name)
                    DetailRow("Vendor",      info.vendor)
                    DetailRow("Version",     "${info.version}")
                    DetailRow("Max rate",    "${info.maxSampleRateHz} Hz")
                    DetailRow("Range",       "±${"%.2f".format(info.maxRangeRaw)}")
                    DetailRow("Resolution",  "${"%.6f".format(info.resolutionRaw)}")
                }

                // Description card
                DetailSection("ABOUT THIS SENSOR") {
                    Text(
                        sensorDescription(sensorKey),
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Output format card
                DetailSection("OUTPUT FORMAT") {
                    Text(
                        sensorOutputFormat(sensorKey),
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                }

                // UDP field names card
                DetailSection("UDP CSV FIELD NAMES") {
                    Surface(
                        shape    = MaterialTheme.shapes.medium,
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            sensorCsvFields(sensorKey),
                            fontSize   = 14.sp,
                            lineHeight = 22.sp,
                            color      = MaterialTheme.colorScheme.onBackground,
                            modifier   = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        Surface(
            shape    = MaterialTheme.shapes.medium,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content             = content
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

// ── Static content per sensor ─────────────────────────────────────────────────

private fun sensorDescription(key: String): String = when (key) {
    "Accelerometer" ->
        "Measures the acceleration force applied to the device on all three axes (x, y, z), " +
        "including the force of gravity. When the device is lying flat, z-axis reads ~9.8 m/s². " +
        "Useful for detecting motion, tilt, shake, and orientation."
    "Gyroscope" ->
        "Measures the rate of rotation around each axis in radians per second. " +
        "Unlike the accelerometer, it is not affected by gravity or linear motion. " +
        "Ideal for detecting rotation speed and angular position changes."
    "Magnetometer" ->
        "Measures the ambient magnetic field on all three axes in microteslas (µT). " +
        "Used as a digital compass to determine the device's orientation relative to " +
        "magnetic north. Sensitive to nearby magnetic interference."
    "Gravity" ->
        "A software-derived sensor that separates the gravitational component from the " +
        "accelerometer reading. Always points toward the ground with magnitude ~9.8 m/s². " +
        "Requires Pro tier to stream. Useful for orientation-relative motion analysis."
    "Linear Acceleration" ->
        "A software-derived sensor that removes gravity from the accelerometer reading, " +
        "leaving only user-induced acceleration. Reads ~0 when the device is stationary. " +
        "Requires Pro tier to stream. Ideal for motion detection without gravity interference."
    "Rotation Vector" ->
        "A software-derived sensor that represents the device orientation as a quaternion " +
        "(x, y, z, w). Fuses accelerometer, gyroscope and magnetometer for high accuracy. " +
        "Requires Pro tier to stream. The w component is included in UDP stream only."
    else -> "No description available."
}

private fun sensorOutputFormat(key: String): String = when (key) {
    "Accelerometer"       -> "3 floats: ax, ay, az  (m/s²)\nRange: ±156.9 m/s² typical"
    "Gyroscope"           -> "3 floats: gx, gy, gz  (rad/s)\nRange: ±34.9 rad/s typical"
    "Magnetometer"        -> "3 floats: mx, my, mz  (µT)\nRange: ±4915 µT typical"
    "Gravity"             -> "3 floats: gravx, gravy, gravz  (m/s²)\nMagnitude always ≈ 9.8"
    "Linear Acceleration" -> "3 floats: lax, lay, laz  (m/s²)\nReads ~0 when stationary"
    "Rotation Vector"     -> "4 floats: rotx, roty, rotz, rotw  (unit quaternion)\n" +
                              "x,y,z shown on screen · w included in UDP stream only"
    else -> ""
}

private fun sensorCsvFields(key: String): String = when (key) {
    "Accelerometer"       -> "timestamp_ms, ax, ay, az"
    "Gyroscope"           -> "timestamp_ms, gx, gy, gz"
    "Magnetometer"        -> "timestamp_ms, mx, my, mz"
    "Gravity"             -> "timestamp_ms, gravx, gravy, gravz"
    "Linear Acceleration" -> "timestamp_ms, lax, lay, laz"
    "Rotation Vector"     -> "timestamp_ms, rotx, roty, rotz, rotw"
    else -> ""
}
