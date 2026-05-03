package com.daqmobile.imulink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black     = Color(0xFF000000)
val StreamGreen = Color(0xFF00E676)  // bright green — visible from distance
val White     = Color(0xFFFFFFFF)

// ── TUNING KNOB ──────────────────────────────────────────────────────────────
// Raise this value to make ALL gray elements lighter.
// 0 = pure black shift (very dark grays)
// 40 = current default (subtle dark grays)
// 80 = noticeably lighter grays
// 120 = medium grays
// Each gray step is offset from this base.
private const val GRAY_LIFT = 60

private fun gray(base: Int) = Color(
    red   = (base + GRAY_LIFT).coerceIn(0, 255),
    green = (base + GRAY_LIFT).coerceIn(0, 255),
    blue  = (base + GRAY_LIFT).coerceIn(0, 255)
)

val Gray900   = gray(17)   // darkest surface
val Gray800   = gray(28)   // card / surface background
val Gray700   = gray(42)   // buttons, input fields
val Gray600   = gray(58)   // dividers, borders
val Gray500   = gray(85)   // disabled elements
val Gray400   = gray(136)  // secondary text
val Gray300   = gray(170)  // hint text
val Gray200   = gray(204)  // light labels
val Gray100   = gray(232)  // near-white

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
