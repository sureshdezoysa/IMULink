package com.daqmobile.imulink.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqmobile.imulink.R
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.Validator
import com.daqmobile.imulink.sensor.AxisData
import com.daqmobile.imulink.sensor.ImuSample
import com.daqmobile.imulink.ui.MainViewModel
import com.daqmobile.imulink.ui.StreamState
import com.daqmobile.imulink.ui.components.SensorRow
import com.daqmobile.imulink.ui.theme.StreamGreen

val StreamingGreen = StreamGreen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val sample        by viewModel.displaySample.collectAsState()
    val streamState   by viewModel.streamState.collectAsState()
    val countdown     by viewModel.countdownSecs.collectAsState()
    val deviceIp      by viewModel.deviceIp.collectAsState()
    val settings      by viewModel.settings.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val popupMessage  by viewModel.popupMessage.collectAsState()
    val hasNetwork    by viewModel.hasNetwork.collectAsState()
    val dataRateBps   by viewModel.dataRateBps.collectAsState()
    val orientation    = LocalConfiguration.current.orientation

    val isStreaming = streamState != StreamState.IDLE
    val canStream   = hasNetwork &&
                      Validator.isValidIp(settings.receiverIp) &&
                      Validator.isValidPort(settings.udpPort)

    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(
            sample, streamState, countdown, deviceIp,
            settings, statusMessage, popupMessage,
            isStreaming, canStream, dataRateBps,
            { viewModel.toggleStreaming() },
            { viewModel.showPopup(it) },
            onNavigateToSettings, onNavigateToHelp
        )
    } else {
        PortraitLayout(
            sample, streamState, countdown, deviceIp,
            settings, statusMessage, popupMessage,
            isStreaming, canStream, dataRateBps,
            { viewModel.toggleStreaming() },
            { viewModel.showPopup(it) },
            onNavigateToSettings, onNavigateToHelp
        )
    }
}

@Composable
private fun PortraitLayout(
    sample: ImuSample, streamState: StreamState, countdown: Int,
    deviceIp: String, settings: AppSettings,
    statusMessage: String, popupMessage: String,
    isStreaming: Boolean, canStream: Boolean, dataRateBps: Int,
    onStartStop: () -> Unit, onShowPopup: (String) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        TopBar(onSettings, onHelp)
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SensorBlock(sample, settings, isStreaming)
        }
        BottomControls(
            streamState, countdown, deviceIp, settings.isPro,
            statusMessage, popupMessage, isStreaming, canStream,
            dataRateBps, onStartStop, onShowPopup
        )
    }
}

@Composable
private fun LandscapeLayout(
    sample: ImuSample, streamState: StreamState, countdown: Int,
    deviceIp: String, settings: AppSettings,
    statusMessage: String, popupMessage: String,
    isStreaming: Boolean, canStream: Boolean, dataRateBps: Int,
    onStartStop: () -> Unit, onShowPopup: (String) -> Unit,
    onSettings: () -> Unit, onHelp: () -> Unit
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
            TopBar(onSettings, onHelp)
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
                streamState, countdown, deviceIp, settings.isPro,
                statusMessage, popupMessage, isStreaming, canStream,
                dataRateBps, onStartStop, onShowPopup
            )
        }
    }
}

/**
 * Sensor display rules:
 * - Free tier:  last 3 sensors always grayed (never green, never white)
 * - Pro tier:   disabled sensors grayed, enabled+streaming = green
 */
@Composable
private fun SensorBlock(
    sample: ImuSample,
    settings: AppSettings,
    isStreaming: Boolean
) {
    // Helper: determine color state for each sensor
    fun activeFor(enabled: Boolean, proLocked: Boolean): Boolean {
        if (proLocked && !settings.isPro) return false  // locked for free users
        if (!enabled) return false                       // user unchecked it
        return isStreaming
    }

    fun dimFor(enabled: Boolean, proLocked: Boolean): Boolean {
        if (proLocked && !settings.isPro) return true
        if (!enabled) return true
        return false
    }

    SensorRow(
        stringResource(R.string.sensor_accelerometer),
        stringResource(R.string.unit_ms2),
        sample.accelerometer,
        isStreaming = activeFor(settings.enableAccelerometer, false),
        dimmed      = dimFor(settings.enableAccelerometer, false)
    )
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_gyroscope),
        stringResource(R.string.unit_rads),
        sample.gyroscope,
        isStreaming = activeFor(settings.enableGyroscope, false),
        dimmed      = dimFor(settings.enableGyroscope, false)
    )
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_magnetometer),
        stringResource(R.string.unit_ut),
        sample.magnetometer,
        isStreaming = activeFor(settings.enableMagnetometer, false),
        dimmed      = dimFor(settings.enableMagnetometer, false)
    )
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_gravity),
        stringResource(R.string.unit_ms2),
        sample.gravity,
        isStreaming = activeFor(settings.enableGravity, proLocked = true),
        dimmed      = dimFor(settings.enableGravity, proLocked = true)
    )
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_linear_accel),
        stringResource(R.string.unit_ms2),
        sample.linearAcceleration,
        isStreaming = activeFor(settings.enableLinearAccel, proLocked = true),
        dimmed      = dimFor(settings.enableLinearAccel, proLocked = true)
    )
    Spacer(Modifier.height(4.dp))
    SensorRow(
        stringResource(R.string.sensor_rotation_vector),
        stringResource(R.string.unit_rad),
        AxisData(sample.rotation.x, sample.rotation.y, sample.rotation.z),
        isStreaming = activeFor(settings.enableRotation, proLocked = true),
        dimmed      = dimFor(settings.enableRotation, proLocked = true)
    )
}

@Composable
private fun TopBar(onSettings: () -> Unit, onHelp: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tappable title → Help
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { onHelp() }
        ) {
            Text(
                text  = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        AnimatedIconButton(
            icon               = Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            onClick            = onSettings
        )
    }
}

@Composable
private fun AnimatedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale   by animateFloatAsState(
        targetValue   = if (pressed) 0.78f else 1f,
        animationSpec = tween(120), label = "scale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (pressed) 0.18f else 0f,
        animationSpec = tween(120), label = "bg"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onClick() }
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = Color.White,
            modifier           = Modifier.size(26.dp)
        )
    }
    LaunchedEffect(pressed) {
        if (pressed) { kotlinx.coroutines.delay(180); pressed = false }
    }
}

@Composable
private fun BottomControls(
    streamState: StreamState, countdown: Int,
    deviceIp: String, isPro: Boolean,
    statusMessage: String, popupMessage: String,
    isStreaming: Boolean, canStream: Boolean, dataRateBps: Int,
    onStartStop: () -> Unit, onShowPopup: (String) -> Unit
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

        if (statusMessage.isNotEmpty()) {
            val display = if (dataRateBps > 0)
                "$statusMessage  ·  ${formatDataRate(dataRateBps)}" else statusMessage
            Text(
                text     = display,
                style    = MaterialTheme.typography.bodyMedium,
                color    = StreamingGreen,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            )
        }

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
                    containerColor = if (isStreaming) StreamingGreen
                                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                val label = when {
                    !isStreaming  -> stringResource(R.string.start_streaming)
                    countdown > 0 -> "STOP  ${formatCountdown(countdown)}"
                    else          -> stringResource(R.string.stop_streaming)
                }
                Text(label, style = MaterialTheme.typography.labelLarge,
                    color = if (isStreaming) Color.Black
                            else MaterialTheme.colorScheme.onBackground)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text  = "${stringResource(R.string.this_device)}  $deviceIp",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCountdown(secs: Int) = "%d:%02d".format(secs / 60, secs % 60)
private fun formatDataRate(bps: Int): String = when {
    bps >= 1_000_000 -> "${"%.1f".format(bps / 1_000_000f)} MB/s"
    bps >= 1_000     -> "${"%.1f".format(bps / 1_000f)} KB/s"
    else             -> "$bps B/s"
}
