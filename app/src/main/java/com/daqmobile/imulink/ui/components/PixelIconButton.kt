package com.daqmobile.imulink.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState

// Pixel-style button colors
private val BgResting = Color(0xFF2C2C2C)
private val BgPressed = Color(0xFF4A4A4A)

/**
 * Gray circle button with white icon.
 * Uses detectTapGestures so the pressed state is visible
 * BEFORE the click action fires — unlike clickable{} which
 * fires onClick immediately without showing the press state.
 */
@Composable
fun PixelIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.84f else 1f,
        animationSpec = tween(100),
        label         = "pixel_scale"
    )
    val bg by animateColorAsState(
        targetValue   = if (pressed) BgPressed else BgResting,
        animationSpec = tween(100),
        label         = "pixel_bg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color = bg, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        // Wait for finger lift (or cancel)
                        val released = tryAwaitRelease()
                        pressed = false
                        if (released) onClick()
                    }
                )
            }
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = contentDescription,
            tint               = Color.White,
            modifier           = Modifier.size(22.dp)
        )
    }
}
