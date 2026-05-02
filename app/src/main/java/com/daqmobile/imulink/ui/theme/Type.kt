package com.daqmobile.imulink.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SpaceMono = FontFamily(
    Font(com.daqmobile.imulink.R.font.space_mono_regular, FontWeight.Normal),
    Font(com.daqmobile.imulink.R.font.space_mono_bold,    FontWeight.Bold)
)

val IMULinkTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        letterSpacing = 1.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        letterSpacing = 2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        letterSpacing = 0.5.sp
    )
)
