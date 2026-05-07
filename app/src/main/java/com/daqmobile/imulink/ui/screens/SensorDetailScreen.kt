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
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val sensorInfos = viewModel.imuRepository.sensorInfos

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Sensor Information",
                        style = MaterialTheme.typography.headlineLarge)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            val sensors = listOf(
                "Accelerometer",
                "Gyroscope",
                "Magnetometer",
                "Gravity",
                "Linear Acceleration",
                "Rotation Vector"
            )

            sensors.forEach { key ->
                val info = sensorInfos[key]
                SensorHardwareCard(label = key, info = info)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SensorHardwareCard(label: String, info: SensorInfo?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        Surface(
            shape    = MaterialTheme.shapes.medium,
            color    = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (info == null || !info.available) {
                Text(
                    text     = "Not available on this device",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            } else {
                Column(
                    modifier            = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HwRow("Model",      info.name)
                    HwRow("Vendor",     info.vendor)
                    HwRow("Version",    "${info.version}")
                    HwRow("Max rate",   "${info.maxSampleRateHz} Hz")
                    HwRow("Range",      "±${"%.2f".format(info.maxRangeRaw)}")
                    HwRow("Resolution", "${"%.6f".format(info.resolutionRaw)}")
                }
            }
        }
    }
}

@Composable
private fun HwRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground)
    }
}
