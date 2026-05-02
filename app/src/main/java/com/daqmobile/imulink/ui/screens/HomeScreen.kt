package com.daqmobile.imulink.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqmobile.imulink.sensor.ImuSample
import com.daqmobile.imulink.ui.MainViewModel
import com.daqmobile.imulink.ui.StreamState
import com.daqmobile.imulink.ui.components.SensorRow

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val sample      by viewModel.displaySample.collectAsState()
    val streamState by viewModel.streamState.collectAsState()
    val countdown   by viewModel.countdownSecs.collectAsState()
    val deviceIp    by viewModel.deviceIp.collectAsState()
    val settings    by viewModel.settings.collectAsState()
    val orientation = LocalConfiguration.current.orientation

    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(sample, streamState, countdown, deviceIp, settings.isPro,
            { viewModel.toggleStreaming() }, onNavigateToSettings, onNavigateToHelp)
    } else {
        PortraitLayout(sample, streamState, countdown, deviceIp, settings.isPro,
            { viewModel.toggleStreaming() }, onNavigateToSettings, onNavigateToHelp)
    }
}

@Composable
private fun PortraitLayout(
    sample: ImuSample, streamState: StreamState, countdown: Int,
    deviceIp: String, isPro: Boolean,
    onStartStop: () -> Unit, onSettings: () -> Unit, onHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        TopBar(streamState, onSettings, onHelp)
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SensorRow(label = "ACCELEROMETER", unit = "m/s²",  data = sample.accelerometer)
            Spacer(Modifier.height(8.dp))
            SensorRow(label = "GYROSCOPE",     unit = "rad/s", data = sample.gyroscope)
            Spacer(Modifier.height(8.dp))
            SensorRow(label = "MAGNETOMETER",  unit = "µT",    data = sample.magnetometer)
        }
        BottomControls(streamState, countdown, deviceIp, isPro, onStartStop)
    }
}

@Composable
private fun LandscapeLayout(
    sample: ImuSample, streamState: StreamState, countdown: Int,
    deviceIp: String, isPro: Boolean,
    onStartStop: () -> Unit, onSettings: () -> Unit, onHelp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f).fillMaxHeight()
                .padding(start = 20.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(streamState, onSettings, onHelp)
            Spacer(Modifier.height(12.dp))
            SensorRow(label = "ACCELEROMETER", unit = "m/s²",  data = sample.accelerometer)
            Spacer(Modifier.height(4.dp))
            SensorRow(label = "GYROSCOPE",     unit = "rad/s", data = sample.gyroscope)
            Spacer(Modifier.height(4.dp))
            SensorRow(label = "MAGNETOMETER",  unit = "µT",    data = sample.magnetometer)
        }
        VerticalDivider(
            modifier  = Modifier.fillMaxHeight().padding(vertical = 16.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outline
        )
        Column(
            modifier = Modifier
                .weight(0.65f).fillMaxHeight()
                .padding(start = 16.dp, end = 20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(Modifier.weight(1f))
            BottomControls(streamState, countdown, deviceIp, isPro, onStartStop)
        }
    }
}

@Composable
private fun TopBar(streamState: StreamState, onSettings: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = "IMULink",
            style    = MaterialTheme.typography.headlineLarge,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        StatusBadge(streamState)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onHelp) {
            Icon(Icons.Outlined.Info, "Help",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSettings) {
            Icon(Icons.Outlined.Settings, "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusBadge(state: StreamState) {
    val label = when (state) {
        StreamState.IDLE      -> "IDLE"
        StreamState.STREAMING -> "STREAMING"
        StreamState.COUNTDOWN -> "STREAMING"
    }
    val color = when (state) {
        StreamState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
        else             -> MaterialTheme.colorScheme.onBackground
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("● ", style = MaterialTheme.typography.labelMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun BottomControls(
    streamState: StreamState, countdown: Int,
    deviceIp: String, isPro: Boolean, onStartStop: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick  = { },
                enabled  = isPro,
                modifier = Modifier.size(64.dp),
                shape    = MaterialTheme.shapes.medium,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("⚡", fontSize = 22.sp,
                    color = if (isPro) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            }
            Button(
                onClick  = onStartStop,
                modifier = Modifier.weight(1f).height(64.dp),
                shape    = MaterialTheme.shapes.medium,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                val label = when {
                    streamState == StreamState.IDLE -> "START STREAMING"
                    countdown > 0 -> "STOP  ${formatCountdown(countdown)}"
                    else -> "STOP STREAMING"
                }
                Text(label, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Text(
            text     = "This device: $deviceIp",
            style    = MaterialTheme.typography.bodyLarge,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 8.dp)
        )
    }
}

private fun formatCountdown(secs: Int) = "%d:%02d".format(secs / 60, secs % 60)
