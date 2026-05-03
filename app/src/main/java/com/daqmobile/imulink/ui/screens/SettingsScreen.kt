package com.daqmobile.imulink.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.daqmobile.imulink.R
import com.daqmobile.imulink.data.AppSettings
import com.daqmobile.imulink.data.Validator
import com.daqmobile.imulink.data.buildSampleRateOptions
import com.daqmobile.imulink.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onSensorDetail: (String) -> Unit
) {
    val settings    by viewModel.settings.collectAsState()
    val scope        = rememberCoroutineScope()

    var receiverIp   by remember { mutableStateOf(settings.receiverIp) }
    var udpPort      by remember { mutableStateOf(settings.udpPort.toString()) }
    var sampleRateHz by remember { mutableStateOf(settings.sampleRateHz) }
    var customRate   by remember { mutableStateOf(settings.sampleRateHz.toString()) }
    var dropdownOpen by remember { mutableStateOf(false) }
    var enAccel      by remember { mutableStateOf(settings.enableAccelerometer) }
    var enGyro       by remember { mutableStateOf(settings.enableGyroscope) }
    var enMag        by remember { mutableStateOf(settings.enableMagnetometer) }
    var enGravity    by remember { mutableStateOf(settings.enableGravity) }
    var enLinear     by remember { mutableStateOf(settings.enableLinearAccel) }
    var enRotation   by remember { mutableStateOf(settings.enableRotation) }
    var saved        by remember { mutableStateOf(true) }

    val ipValid   = Validator.isValidIp(receiverIp)
    val portValid = Validator.isValidPort(udpPort)
    val canSave   = ipValid && portValid

    val sensorInfos = viewModel.imuRepository.sensorInfos
    val maxSensorHz = sensorInfos["Accelerometer"]
        ?.takeIf { it.available }?.maxSampleRateHz ?: 200
    val rateOptions = buildSampleRateOptions(maxSensorHz)
    val freeMax     = 50

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack,
                            stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onHelp) {
                        Icon(Icons.Outlined.HelpOutline,
                            stringResource(R.string.cd_help),
                            tint     = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(26.dp))
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

            // ── CONNECTION ────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_connection)) {
                // IP + Port row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value           = receiverIp,
                            onValueChange   = { receiverIp = it; saved = false },
                            label           = { Text(stringResource(R.string.settings_ip_address), fontSize = 12.sp) },
                            placeholder     = { Text("192.168.1.x", fontSize = 13.sp) },
                            singleLine      = true,
                            isError         = receiverIp.isNotEmpty() && !ipValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle       = fieldTextStyle(saved),
                            modifier        = Modifier.fillMaxWidth(),
                            colors          = fieldColors(saved)
                        )
                        if (receiverIp.isNotEmpty() && !ipValid) {
                            Text(stringResource(R.string.settings_invalid_ip_short), fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    Column(modifier = Modifier.width(88.dp)) {
                        OutlinedTextField(
                            value           = udpPort,
                            onValueChange   = { udpPort = it; saved = false },
                            label           = { Text("Port", fontSize = 12.sp) },
                            placeholder     = { Text("5005", fontSize = 13.sp) },
                            singleLine      = true,
                            isError         = udpPort.isNotEmpty() && !portValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle       = fieldTextStyle(saved),
                            colors          = fieldColors(saved)
                        )
                        if (udpPort.isNotEmpty() && !portValid) {
                            Text(stringResource(R.string.settings_invalid_port_short), fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                // Sample rate — full width
                if (settings.isPro) {
                    OutlinedTextField(
                        value           = customRate,
                        onValueChange   = {
                            customRate = it
                            it.toIntOrNull()?.let { v -> sampleRateHz = v }
                            saved = false
                        },
                        label           = { Text("Sample rate (Hz)", fontSize = 12.sp) },
                        placeholder     = { Text("1 – $maxSensorHz", fontSize = 13.sp) },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle       = fieldTextStyle(saved),
                        modifier        = Modifier.fillMaxWidth(),
                        colors          = fieldColors(saved)
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded         = dropdownOpen,
                        onExpandedChange = { dropdownOpen = it },
                        modifier         = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value         = "$sampleRateHz",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Sample rate (Hz)", fontSize = 12.sp) },
                            trailingIcon  = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen)
                            },
                            textStyle     = fieldTextStyle(saved),
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            colors        = fieldColors(saved)
                        )
                        ExposedDropdownMenu(
                            expanded         = dropdownOpen,
                            onDismissRequest = { dropdownOpen = false }
                        ) {
                            rateOptions.forEach { hz ->
                                val isFree = hz <= freeMax
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier              = Modifier.fillMaxWidth()
                                        ) {
                                            // Number only — no "Hz" suffix
                                            Text(
                                                text     = "$hz",
                                                fontSize = 14.sp,
                                                color    = if (isFree)
                                                    MaterialTheme.colorScheme.onBackground
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                        .copy(alpha = 0.5f)
                                            )
                                            if (!isFree) {
                                                Icon(Icons.Outlined.Lock, "Pro",
                                                    tint     = MaterialTheme.colorScheme
                                                        .onSurfaceVariant.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (isFree) {
                                            sampleRateHz = hz
                                            dropdownOpen = false
                                            saved = false
                                        }
                                    },
                                    enabled = isFree
                                )
                            }
                        }
                    }
                }
            }

            // ── SENSORS TO STREAM ─────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_sensors)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SensorCard(
                        label    = stringResource(R.string.sensor_accelerometer),
                        checked  = enAccel,
                        locked   = false,
                        onChange = { enAccel = it; saved = false },
                        onTap    = { onSensorDetail("Accelerometer") }
                    )
                    SensorCard(
                        label    = stringResource(R.string.sensor_gyroscope),
                        checked  = enGyro,
                        locked   = false,
                        onChange = { enGyro = it; saved = false },
                        onTap    = { onSensorDetail("Gyroscope") }
                    )
                    SensorCard(
                        label    = stringResource(R.string.sensor_magnetometer),
                        checked  = enMag,
                        locked   = false,
                        onChange = { enMag = it; saved = false },
                        onTap    = { onSensorDetail("Magnetometer") }
                    )
                    SensorCard(
                        label    = stringResource(R.string.sensor_gravity),
                        checked  = enGravity,
                        locked   = !settings.isPro,
                        onChange = { if (settings.isPro) { enGravity = it; saved = false } },
                        onTap    = { onSensorDetail("Gravity") }
                    )
                    SensorCard(
                        label    = stringResource(R.string.sensor_linear_accel),
                        checked  = enLinear,
                        locked   = !settings.isPro,
                        onChange = { if (settings.isPro) { enLinear = it; saved = false } },
                        onTap    = { onSensorDetail("Linear Acceleration") }
                    )
                    SensorCard(
                        label    = stringResource(R.string.sensor_rotation_vector),
                        checked  = enRotation,
                        locked   = !settings.isPro,
                        onChange = { if (settings.isPro) { enRotation = it; saved = false } },
                        onTap    = { onSensorDetail("Rotation Vector") }
                    )
                }
            }

            // ── SAVE ──────────────────────────────────────────────────────
            AnimatedSaveButton(canSave = canSave, saved = saved, onClick = {
                scope.launch {
                    val finalRate = if (settings.isPro)
                        customRate.toIntOrNull()?.coerceIn(1, maxSensorHz) ?: 50
                    else sampleRateHz
                    viewModel.settingsRepository.save(AppSettings(
                        receiverIp          = receiverIp.trim(),
                        udpPort             = udpPort.toIntOrNull() ?: 5005,
                        sampleRateHz        = finalRate,
                        runTimeSecs         = settings.runTimeSecs,
                        isPro               = settings.isPro,
                        enableAccelerometer = enAccel,
                        enableGyroscope     = enGyro,
                        enableMagnetometer  = enMag,
                        enableGravity       = enGravity && settings.isPro,
                        enableLinearAccel   = enLinear && settings.isPro,
                        enableRotation      = enRotation && settings.isPro
                    ))
                    saved = true
                }
            })

            // ── APP INFO ──────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.settings_section_app)) {
                InfoRow(stringResource(R.string.settings_version), "1.0.0")
                InfoRow(stringResource(R.string.settings_package), "com.daqmobile.imulink")
                InfoRow(
                    stringResource(R.string.settings_license),
                    if (settings.isPro) stringResource(R.string.settings_license_pro)
                    else stringResource(R.string.settings_license_free)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SensorCard(
    label:    String,
    checked:  Boolean,
    locked:   Boolean,
    onChange: (Boolean) -> Unit,
    onTap:    () -> Unit
) {
    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onTap() }
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked         = checked && !locked,
                onCheckedChange = if (locked) null else onChange,
                enabled         = !locked,
                colors          = CheckboxDefaults.colors(
                    checkedColor           = MaterialTheme.colorScheme.onBackground,
                    uncheckedColor         = MaterialTheme.colorScheme.outline,
                    disabledCheckedColor   = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    checkmarkColor         = MaterialTheme.colorScheme.background
                )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = label,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                color    = if (locked)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.onBackground
            )
            if (locked) {
                Icon(Icons.Outlined.Lock, "Pro only",
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Details",
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun fieldTextStyle(saved: Boolean) =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        color    = if (saved)
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        else Color.White
    )

@Composable
private fun fieldColors(saved: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    errorContainerColor     = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor      = if (saved) MaterialTheme.colorScheme.outline
                              else MaterialTheme.colorScheme.onBackground,
    unfocusedBorderColor    = if (saved) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                              else MaterialTheme.colorScheme.outline,
    errorBorderColor        = MaterialTheme.colorScheme.error,
    focusedLabelColor       = MaterialTheme.colorScheme.onBackground,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLabelColor         = MaterialTheme.colorScheme.error,
    focusedTextColor        = if (saved)
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f) else Color.White,
    unfocusedTextColor      = if (saved)
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f) else Color.White,
    cursorColor             = Color.White
)

@Composable
private fun AnimatedSaveButton(canSave: Boolean, saved: Boolean, onClick: () -> Unit) {
    val isActive = canSave && !saved
    var pressed  by remember { mutableStateOf(false) }
    val bgColor  by animateColorAsState(
        targetValue   = when {
            !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            pressed   -> Color.White
            else      -> MaterialTheme.colorScheme.surfaceVariant
        }, animationSpec = tween(150), label = "save_bg"
    )
    val textColor by animateColorAsState(
        targetValue   = when {
            !isActive -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            pressed   -> Color.Black
            else      -> MaterialTheme.colorScheme.onBackground
        }, animationSpec = tween(150), label = "save_text"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth().height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = isActive
            ) { pressed = true; onClick() }
    ) {
        Text("SAVE", style = MaterialTheme.typography.labelLarge, color = textColor)
    }
    LaunchedEffect(pressed) {
        if (pressed) { kotlinx.coroutines.delay(200); pressed = false }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}
