package com.daqmobile.imulink.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingPage(
    val title:       String,
    val subtitle:    String,
    val body:        String,
    val icon:        String,
    val showSettingsHint: Boolean = false
)

private val pages = listOf(
    OnboardingPage(
        icon     = "〜",
        title    = "Welcome to IMULink",
        subtitle = "Stream your phone's sensors to any computer",
        body     = "IMULink streams real-time IMU data — accelerometer, gyroscope, " +
                   "magnetometer and more — over Wi-Fi UDP to any receiver on your network. " +
                   "Free and open for science."
    ),
    OnboardingPage(
        icon              = "⚙",
        title             = "Set up your receiver",
        subtitle          = "Tell the app where to send data",
        body              = "Open Settings and enter your computer's IP address and UDP port. " +
                            "Make sure both devices are on the same Wi-Fi network. " +
                            "Your phone's IP is shown at the bottom of the home screen.",
        showSettingsHint  = true
    ),
    OnboardingPage(
        icon     = "▶",
        title    = "Start streaming",
        subtitle = "One tap to send live sensor data",
        body     = "Press START STREAMING on the home screen. " +
                   "Data arrives at your computer as CSV over UDP — " +
                   "ready for Python, MATLAB, C#, Unity or any custom receiver. " +
                   "Open Help & Guide for code examples."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val isLastPage   = currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Skip button — top right, hidden on last page
        if (!isLastPage) {
            Text(
                text  = "Skip",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF888888),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onFinish() }
            )
        }

        // Page content
        AnimatedContent(
            targetState   = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300))
                } else {
                    slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                }
            },
            label = "onboarding_page"
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large icon
                Text(
                    text      = p.icon,
                    fontSize  = 64.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(bottom = 40.dp)
                )

                // Title
                Text(
                    text      = p.title,
                    style     = MaterialTheme.typography.headlineLarge,
                    color     = Color.White,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(bottom = 12.dp)
                )

                // Subtitle
                Text(
                    text      = p.subtitle,
                    fontSize  = 15.sp,
                    color     = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(bottom = 24.dp)
                )

                // Body
                Text(
                    text       = p.body,
                    fontSize   = 14.sp,
                    color      = Color.White.copy(alpha = 0.55f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier   = Modifier.padding(bottom = if (p.showSettingsHint) 32.dp else 0.dp)
                )

                // Settings hint — visible only on slide 2
                if (p.showSettingsHint) {
                    SettingsHint()
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val isActive = index == currentPage
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) Color.White else Color(0xFF444444)
                            )
                    )
                }
            }

            // Next / Get Started button
            Button(
                onClick = {
                    if (isLastPage) onFinish()
                    else currentPage++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape  = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text  = if (isLastPage) "GET STARTED" else "NEXT",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Non-clickable visual hint showing where the Settings button is.
 * Mimics the top bar of HomeScreen so user knows exactly what to tap.
 */
@Composable
private fun SettingsHint() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text     = "Find Settings here ↓",
            fontSize = 12.sp,
            color    = Color.White.copy(alpha = 0.5f)
        )

        // Mock top bar — not clickable, just illustrative
        Surface(
            shape    = MaterialTheme.shapes.medium,
            color    = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "IMULink",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontSize = 22.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ? button mock
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C))
                    ) {
                        Text("?", color = Color.White, fontSize = 14.sp)
                    }
                    // Settings button mock — highlighted with a ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3A))
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Text(
            text     = "Tap ⚙ to open Settings",
            fontSize = 12.sp,
            color    = Color.White.copy(alpha = 0.5f)
        )
    }
}
