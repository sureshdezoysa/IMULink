package com.daqmobile.imulink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black     = Color(0xFF000000)
val White     = Color(0xFFFFFFFF)
val Gray900   = Color(0xFF111111)
val Gray800   = Color(0xFF1C1C1C)
val Gray700   = Color(0xFF2A2A2A)
val Gray600   = Color(0xFF3A3A3A)
val Gray500   = Color(0xFF555555)
val Gray400   = Color(0xFF888888)
val Gray300   = Color(0xFFAAAAAA)
val Gray200   = Color(0xFFCCCCCC)
val Gray100   = Color(0xFFE8E8E8)

private val DarkColors = darkColorScheme(
    primary             = White,
    onPrimary           = Black,
    primaryContainer    = Gray700,
    onPrimaryContainer  = White,
    secondary           = Gray300,
    onSecondary         = Black,
    background          = Black,
    onBackground        = White,
    surface             = Gray800,
    onSurface           = White,
    surfaceVariant      = Gray700,
    onSurfaceVariant    = Gray300,
    outline             = Gray600,
    outlineVariant      = Gray700,
)

@Composable
fun IMULinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = IMULinkTypography,
        content     = content
    )
}
