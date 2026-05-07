package com.daqmobile.imulink.ui.screens

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daqmobile.imulink.R
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.Validator
import com.daqmobile.imulink.sensor.AxisData
import com.daqmobile.imulink.sensor.ImuSample
import com.daqmobile.imulink.ui.MainViewModel
import com.daqmobile.imulink.ui.StreamState
import com.daqmobile.imulink.ui.components.PixelIconButton
import com.daqmobile.imulink.ui.components.SensorRow
import com.daqmobile.imulink.ui.theme.StreamGreen

private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.daqmobile.imulink"

val StreamingGreen = StreamGreen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val sample        by viewModel.displaySample.collectAsState()
    val streamState   by viewModel.streamState.collectAsState()
    val deviceIp      by viewModel.deviceIp.collectAsState()
    val settings      by viewModel.settings.collectAsState()
    val popupMessage  by viewModel.popupMessage.collectAsState()
    val hasNetwork    by viewModel.hasNetwork.collectAsState()
    val dataRateBps   by viewModel.dataRateBps.collectAsState()
    val orientation    = LocalConfiguration.current.orientation
    val context        = LocalContext.current

    val isStreaming = streamState != StreamState.IDLE
    val maxSensorHz = viewModel.imuRepository.sensorInfos["Accelerometer"]
        ?.takeIf { it.available }?.maxSampleRateHz ?: 200
    val rateLabel = if (settings.sampleRateHz >= maxSensorHz)
        "${settings.sampleRateHz} Hz (Max)" else "${settings.sampleRateHz} Hz"

    val onPlayStore = {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)))
    }

    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(
            sample, isStreaming, deviceIp, rateLabel, settings,
            popupMessage, dataRateBps,
            { viewModel.toggleStreaming() },
            { viewModel.showPopup(it) },
            onNavigateToSettings, onNavigateToHelp, onPlayStore
        )
    } else {
        PortraitLayout(
            sample, isStreaming, deviceIp, rateLabel, settings,
            popupMessage, dataRateBps,
            { viewModel.toggleStreaming() },
            { viewModel.showPopup(it) },
            onNavigateToSettings, onNavigateToHelp, onPlayStore
        )
    }
}

@Composable
private fun PortraitLayout(
    sample: ImuSample, isStreaming: Boolean,
    deviceIp: String, rateLabel: String,
    settings: AppSettings, popupMessage: String, dataRateBps: Int,
    onStartStop: () -> Unit, onShowPopup: (String) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit, onPlayStore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        TopBar(onSettings, onHelp, onPlayStore)
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SensorBlock(sample, settings, isStreaming)
        }
        BottomControls(
            isStreaming, deviceIp, rateLabel, settings,
            popupMessage, dataRateBps, onStartStop, onShowPopup
        )
    }
}

@Composable
private fun LandscapeLayout(
    sample: ImuSample, isStreaming: Boolean,
    deviceIp: String, rateLabel: String,
    settings: AppSettings, popupMessage: String, dataRateBps: Int,
    onStartStop: () -> Unit, onShowPopup: (String) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit, onPlayStore: () -> Unit
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
        ) {
            TopBar(onSettings, onHelp, onPlayStore)
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SensorBlock(sample, settings, isStreaming)
                Spacer(Modifier.height(8.dp))
            }
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
            BottomControls(
                isStreaming, deviceIp, rateLabel, settings,
                popupMessage, dataRateBps, onStartStop, onShowPopup
            )
        }
    }
}

@Composable
private fun SensorBlock(
    sample: ImuSample, settings: AppSettings, isStreaming: Boolean
) {
    SensorRow(stringResource(R.string.sensor_accelerometer),
        stringResource(R.string.unit_ms2), sample.accelerometer,
        isStreaming = isStreaming && settings.enableAccelerometer,
        dimmed      = !settings.enableAccelerometer)
    Spacer(Modifier.height(4.dp))
    SensorRow(stringResource(R.string.sensor_gyroscope),
        stringResource(R.string.unit_rads), sample.gyroscope,
        isStreaming = isStreaming && settings.enableGyroscope,
        dimmed      = !settings.enableGyroscope)
    Spacer(Modifier.height(4.dp))
    SensorRow(stringResource(R.string.sensor_magnetometer),
        stringResource(R.string.unit_ut), sample.magnetometer,
        isStreaming = isStreaming && settings.enableMagnetometer,
        dimmed      = !settings.enableMagnetometer)
    Spacer(Modifier.height(4.dp))
    SensorRow(stringResource(R.string.sensor_gravity),
        stringResource(R.string.unit_ms2), sample.gravity,
        isStreaming = isStreaming && settings.enableGravity,
        dimmed      = !settings.enableGravity)
    Spacer(Modifier.height(4.dp))
    SensorRow(stringResource(R.string.sensor_linear_accel),
        stringResource(R.string.unit_ms2), sample.linearAcceleration,
        isStreaming = isStreaming && settings.enableLinearAccel,
        dimmed      = !settings.enableLinearAccel)
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_rotation_vector),
        stringResource(R.string.unit_rad),
        AxisData(sample.rotation.x, sample.rotation.y, sample.rotation.z),
        isStreaming = isStreaming && settings.enableRotation,
        dimmed      = !settings.enableRotation)
}

@Composable
private fun TopBar(
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onPlayStore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = stringResource(R.string.app_name),
            style    = MaterialTheme.typography.headlineLarge,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { onPlayStore() }
        )

        PixelIconButton(
            icon               = Icons.Outlined.HelpOutline,
            contentDescription = stringResource(R.string.cd_help),
            onClick            = onHelp
        )
        Spacer(Modifier.width(8.dp))
        PixelIconButton(
            icon               = Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            onClick            = onSettings
        )
    }
}

@Composable
private fun BottomControls(
    isStreaming: Boolean, deviceIp: String, rateLabel: String,
    settings: AppSettings, popupMessage: String,
    dataRateBps: Int, onStartStop: () -> Unit, onShowPopup: (String) -> Unit
) {
    Column {
        AnimatedVisibility(
            visible = popupMessage.isNotEmpty(),
            enter   = fadeIn(tween(200)) + slideInVertically(tween(200)) { it },
            exit    = fadeOut(tween(300))
        ) {
            Surface(
                shape    = MaterialTheme.shapes.medium,
                color    = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text     = popupMessage,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        if (Validator.isValidIp(settings.receiverIp) &&
            Validator.isValidPort(settings.udpPort)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Receiver:  ${settings.receiverIp}:${settings.udpPort}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isStreaming) StreamingGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isStreaming && dataRateBps > 0) {
                    Text(
                        text  = formatDataRate(dataRateBps),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StreamingGreen
                    )
                }
            }
        }

        Button(
            onClick  = onStartStop,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape    = MaterialTheme.shapes.medium,
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (isStreaming) StreamingGreen
                                 else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text  = if (isStreaming) stringResource(R.string.stop_streaming)
                        else stringResource(R.string.start_streaming),
                style = MaterialTheme.typography.labelLarge,
                color = if (isStreaming) Color.Black
                        else MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = "${stringResource(R.string.this_device)}  $deviceIp",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = rateLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDataRate(bps: Int): String = when {
    bps >= 1_000_000 -> "${"%.1f".format(bps / 1_000_000f)} MB/s"
    bps >= 1_000     -> "${"%.1f".format(bps / 1_000f)} KB/s"
    else             -> "$bps B/s"
}
