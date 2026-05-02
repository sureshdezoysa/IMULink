package com.daqmobile.imulink.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.sensor.SensorInfo
import com.daqmobile.imulink.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val settings     by viewModel.settings.collectAsState()
    val sensorInfos   = viewModel.imuRepository.sensorInfos
    val scope         = rememberCoroutineScope()

    var receiverIp   by remember(settings.receiverIp)   { mutableStateOf(settings.receiverIp) }
    var udpPort      by remember(settings.udpPort)      { mutableStateOf(settings.udpPort.toString()) }
    var sampleRateHz by remember(settings.sampleRateHz) { mutableStateOf(settings.sampleRateHz.toString()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineLarge) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SettingsSection("CONNECTION") {
                SettingsField("Receiver IP address", receiverIp, { receiverIp = it },
                    "e.g. 192.168.1.100", KeyboardType.Uri)
                SettingsField("UDP port", udpPort, { udpPort = it },
                    "5005", KeyboardType.Number)
            }

            SettingsSection("SAMPLING") {
                SettingsField("Sample rate (Hz)", sampleRateHz, { sampleRateHz = it },
                    "50", KeyboardType.Number,
                    "Max 50 Hz on free tier · Upgrade for higher speeds")
            }

            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = MaterialTheme.shapes.medium,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant),
                onClick  = {
                    scope.launch {
                        viewModel.settingsRepository.save(AppSettings(
                            receiverIp   = receiverIp,
                            udpPort      = udpPort.toIntOrNull() ?: 5005,
                            sampleRateHz = sampleRateHz.toIntOrNull() ?: 50,
                            runTimeSecs  = settings.runTimeSecs,
                            isPro        = settings.isPro
                        ))
                        onBack()
                    }
                }
            ) {
                Text("SAVE", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            SettingsSection("SENSOR INFORMATION") {
                sensorInfos.forEach { (key, info) ->
                    SensorInfoCard(key, info)
                    Spacer(Modifier.height(8.dp))
                }
            }

            SettingsSection("APP") {
                InfoRow("Version", "1.0.0")
                InfoRow("Package", "com.daqmobile.imulink")
                InfoRow("License", if (settings.isPro) "Pro (Unlimited)" else "Free (5 min limit)")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun SettingsField(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String = "", keyboardType: KeyboardType = KeyboardType.Text,
    helper: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor    = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor          = MaterialTheme.colorScheme.onBackground
            )
        )
        if (helper != null)
            Text(helper, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SensorInfoCard(label: String, info: SensorInfo) {
    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            if (!info.available) {
                Text("Not available on this device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                InfoRow("Model",      info.name)
                InfoRow("Vendor",     info.vendor)
                InfoRow("Max rate",   "${info.maxSampleRateHz} Hz")
                InfoRow("Range",      "±${"%.1f".format(info.maxRangeRaw)}")
                InfoRow("Resolution", "${"%.4f".format(info.resolutionRaw)}")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground)
    }
}
