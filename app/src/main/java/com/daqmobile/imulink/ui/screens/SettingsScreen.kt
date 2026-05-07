package com.daqmobile.imulink.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.HelpOutline
import com.daqmobile.imulink.ui.components.PixelIconButton
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
    onSensorInfo: () -> Unit
) {
    val settings    by viewModel.settings.collectAsState()
    val scope        = rememberCoroutineScope()

    var receiverIp   by remember { mutableStateOf(settings.receiverIp) }
    var udpPort      by remember { mutableStateOf(settings.udpPort.toString()) }
    var sampleRateHz by remember { mutableStateOf(settings.sampleRateHz) }
    var customRate   by remember { mutableStateOf(
        if (settings.sampleRateHz > 0) settings.sampleRateHz.toString() else ""
    )}
    var useCustom    by remember { mutableStateOf(false) }
    var dropdownOpen by remember { mutableStateOf(false) }

    var enAccel   by remember { mutableStateOf(settings.enableAccelerometer) }
    var enGyro    by remember { mutableStateOf(settings.enableGyroscope) }
    var enMag     by remember { mutableStateOf(settings.enableMagnetometer) }
    var enGravity by remember { mutableStateOf(settings.enableGravity) }
    var enLinear  by remember { mutableStateOf(settings.enableLinearAccel) }
    var enRot     by remember { mutableStateOf(settings.enableRotation) }
    var saved     by remember { mutableStateOf(true) }

    val ipValid   = Validator.isValidIp(receiverIp)
    val portValid = Validator.isValidPort(udpPort)

    val sensorInfos = viewModel.imuRepository.sensorInfos
    val maxSensorHz = sensorInfos["Accelerometer"]
        ?.takeIf { it.available }?.maxSampleRateHz ?: 200
    val rateOptions = buildSampleRateOptions(maxSensorHz)

    val customRateInt   = customRate.toIntOrNull()
    val customRateValid = customRateInt != null && customRateInt in 1..maxSensorHz
    val canSave         = ipValid && portValid && (!useCustom || customRateValid)

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
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        PixelIconButton(
                            icon               = Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.cd_help),
                            onClick            = onHelp
                        )
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

                // Row 1: IP + Port
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value           = receiverIp,
                            onValueChange   = { receiverIp = it; saved = false },
                            label           = {
                                Text(stringResource(R.string.settings_ip_address),
                                    fontSize = 12.sp)
                            },
                            placeholder     = {
                                Text(stringResource(R.string.settings_ip_placeholder),
                                    fontSize = 13.sp)
                            },
                            singleLine      = true,
                            isError         = receiverIp.isNotEmpty() && !ipValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle       = fieldTextStyle(saved),
                            modifier        = Modifier.fillMaxWidth(),
                            colors          = fieldColors(saved)
                        )
                        if (receiverIp.isNotEmpty() && !ipValid) {
                            Text(stringResource(R.string.settings_invalid_ip_short),
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    Column(modifier = Modifier.width(88.dp)) {
                        OutlinedTextField(
                            value           = udpPort,
                            onValueChange   = { udpPort = it; saved = false },
                            label           = {
                                Text(stringResource(R.string.settings_port), fontSize = 12.sp)
                            },
                            placeholder     = {
                                Text(stringResource(R.string.settings_port_placeholder),
                                    fontSize = 13.sp)
                            },
                            singleLine      = true,
                            isError         = udpPort.isNotEmpty() && !portValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle       = fieldTextStyle(saved),
                            colors          = fieldColors(saved)
                        )
                        if (udpPort.isNotEmpty() && !portValid) {
                            Text(stringResource(R.string.settings_invalid_port_short),
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                // Row 2: Sample rate (same width as IP) + Custom toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.Top
                ) {
                    // Rate field — same flex weight as IP address above
                    Column(modifier = Modifier.weight(1f)) {
                        if (useCustom) {
                            OutlinedTextField(
                                value           = customRate,
                                onValueChange   = {
                                    if (it.all { c -> c.isDigit() }) {
                                        customRate = it; saved = false
                                    }
                                },
                                label           = {
                                    Text("Rate (1–$maxSensorHz Hz)", fontSize = 12.sp)
                                },
                                placeholder     = { Text("e.g. 75", fontSize = 13.sp) },
                                singleLine      = true,
                                isError         = customRate.isNotEmpty() && !customRateValid,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle       = fieldTextStyle(saved),
                                modifier        = Modifier.fillMaxWidth(),
                                colors          = fieldColors(saved)
                            )
                            if (customRate.isNotEmpty() && !customRateValid) {
                                Text("1 – $maxSensorHz only",
                                    fontSize = 11.sp,
                                    color    = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded         = dropdownOpen,
                                onExpandedChange = { dropdownOpen = it },
                                modifier         = Modifier.fillMaxWidth()
                            ) {
                                val display = if (sampleRateHz >= maxSensorHz)
                                    "$sampleRateHz (Max)" else "$sampleRateHz"
                                OutlinedTextField(
                                    value         = display,
                                    onValueChange = {},
                                    readOnly      = true,
                                    label         = {
                                        Text(stringResource(R.string.settings_sample_rate),
                                            fontSize = 12.sp)
                                    },
                                    trailingIcon  = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = dropdownOpen)
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
                                        val isMax = hz >= maxSensorHz
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (isMax) "$hz (Max)" else "$hz",
                                                    fontSize = 14.sp,
                                                    color    = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                sampleRateHz = hz
                                                dropdownOpen = false
                                                saved = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Custom toggle — same width as Port field for alignment
                    Column(
                        modifier            = Modifier.width(88.dp).padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Custom", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(
                            checked         = useCustom,
                            onCheckedChange = { useCustom = it; saved = false },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = MaterialTheme.colorScheme.onBackground,
                                checkedTrackColor   = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // ── SELECT SENSORS — plain checklist, no card wrapper ─────────
            SettingsSection("SELECT SENSORS") {
                Column {
                    SensorCheckRow(stringResource(R.string.sensor_accelerometer),
                        enAccel,  { enAccel  = it; saved = false })
                    SensorDivider()
                    SensorCheckRow(stringResource(R.string.sensor_gyroscope),
                        enGyro,   { enGyro   = it; saved = false })
                    SensorDivider()
                    SensorCheckRow(stringResource(R.string.sensor_magnetometer),
                        enMag,    { enMag    = it; saved = false })
                    SensorDivider()
                    SensorCheckRow(stringResource(R.string.sensor_gravity),
                        enGravity, { enGravity = it; saved = false })
                    SensorDivider()
                    SensorCheckRow(stringResource(R.string.sensor_linear_accel),
                        enLinear, { enLinear = it; saved = false })
                    SensorDivider()
                    SensorCheckRow(stringResource(R.string.sensor_rotation_vector),
                        enRot,    { enRot    = it; saved = false })
                }
            }

            // ── SAVE ──────────────────────────────────────────────────────
            AnimatedSaveButton(canSave = canSave, saved = saved, onClick = {
                scope.launch {
                    val finalRate = if (useCustom)
                        customRate.toIntOrNull() ?: sampleRateHz
                    else sampleRateHz
                    viewModel.settingsRepository.save(AppSettings(
                        receiverIp          = receiverIp.trim(),
                        udpPort             = udpPort.toIntOrNull() ?: 5005,
                        sampleRateHz        = finalRate,
                        enableAccelerometer = enAccel,
                        enableGyroscope     = enGyro,
                        enableMagnetometer  = enMag,
                        enableGravity       = enGravity,
                        enableLinearAccel   = enLinear,
                        enableRotation      = enRot
                    ))
                    saved = true
                }
            })

            // ── HARDWARE INFO button ──────────────────────────────────────
            OutlinedButton(
                onClick  = onSensorInfo,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = MaterialTheme.shapes.medium,
                border   = androidx.compose.foundation.BorderStroke(
                    0.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Sensor Information",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground)
            }

            // ── APP VERSION ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Version 1.0.0  ·  Free — Open for Science",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun SensorCheckRow(
    label:    String,
    checked:  Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onChange,
            colors          = CheckboxDefaults.colors(
                checkedColor   = MaterialTheme.colorScheme.onBackground,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.background
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SensorDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 4.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
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
        targetValue = when {
            !isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            pressed   -> Color.White
            else      -> MaterialTheme.colorScheme.surfaceVariant
        }, animationSpec = tween(150), label = "save_bg"
    )
    val textColor by animateColorAsState(
        targetValue = when {
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
