package com.daqmobile.imulink.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.daqmobile.imulink.ui.MainViewModel
import com.daqmobile.imulink.ui.StreamState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val streamState   by viewModel.streamState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val dataRateBps   by viewModel.dataRateBps.collectAsState()
    val isStreaming    = streamState != StreamState.IDLE

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text("Help & Guide", style = MaterialTheme.typography.headlineLarge)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor             = MaterialTheme.colorScheme.background,
                        titleContentColor          = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                // Streaming banner — visible only when streaming
                AnimatedVisibility(
                    visible = isStreaming && statusMessage.isNotEmpty(),
                    enter   = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit    = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ) {
                    
                    Surface(
                        color    = StreamingGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pulsing dot
                            Text(
                                text  = "● ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StreamingGreen
                            )
                            Text(
                                text  = statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StreamingGreen
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            HelpSection("WHAT IS IMULINK?") {
                HelpPara(
                    "IMULink streams raw IMU sensor data from your Android phone to any " +
                    "computer on the same Wi-Fi network using UDP. You can record or process " +
                    "the data in Python, MATLAB, C#, or any language that supports UDP sockets."
                )
            }

            HelpSection("QUICK START") {
                HelpStep("1", "Connect your phone and computer to the same Wi-Fi network.")
                HelpStep("2", "Open Settings and enter your computer's IP address and UDP port.")
                HelpStep("3", "Run the receiver script on your computer (see UDP Format below).")
                HelpStep("4", "Press START STREAMING on the home screen.")
                HelpStep("5", "Data will arrive at your computer at the configured sample rate.")
            }

            HelpSection("FINDING YOUR COMPUTER'S IP") {
                HelpPara("macOS / Linux:  open Terminal and type  ifconfig  — look for inet.")
                HelpPara("Windows:  open Command Prompt and type  ipconfig  — look for IPv4 Address.")
                HelpPara("Your phone's own IP is shown at the bottom of the home screen.")
            }

            HelpSection("UDP PACKET FORMAT") {
                HelpPara("Each datagram is one UTF-8 CSV line terminated with a newline:")
                HelpCode(
                    "timestamp_ms, ax,ay,az, gx,gy,gz, mx,my,mz,\n" +
                    "gravx,gravy,gravz, lax,lay,laz, rotx,roty,rotz,rotw\n\n" +
                    "timestamp_ms — Unix time in milliseconds\n" +
                    "ax,ay,az     — Accelerometer  (m/s²)\n" +
                    "gx,gy,gz     — Gyroscope       (rad/s)\n" +
                    "mx,my,mz     — Magnetometer    (µT)\n" +
                    "gravx,y,z    — Gravity          (m/s²)\n" +
                    "lax,y,z      — Linear Accel     (m/s²)\n" +
                    "rotx,y,z,w   — Rotation Vector  (quaternion)"
                )
            }

            HelpSection("PYTHON RECEIVER EXAMPLE") {
                HelpCode(
                    "import socket\n\n" +
                    "sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)\n" +
                    "sock.bind(('0.0.0.0', 5005))\n" +
                    "print('Waiting for IMULink data...')\n" +
                    "while True:\n" +
                    "    data, addr = sock.recvfrom(2048)\n" +
                    "    v = data.decode().strip().split(',')\n" +
                    "    ts = int(v[0])\n" +
                    "    ax, ay, az = float(v[1]), float(v[2]), float(v[3])\n" +
                    "    print(f't={ts} acc=({ax:.3f},{ay:.3f},{az:.3f})')"
                )
            }

            HelpSection("FREE vs PRO") {
                HelpPara("Free tier: streaming stops after 5 minutes. Sample rate capped at 50 Hz.")
                HelpPara("Pro (one-time purchase): unlimited streaming time and full sensor rate.")
            }

            HelpSection("TIPS") {
                HelpStep("•", "Use a 5 GHz Wi-Fi network for lowest latency.")
                HelpStep("•", "Keep the phone plugged in during long recording sessions.")
                HelpStep("•", "The ⚡ button engages top-speed mode (Pro only).")
                HelpStep("•", "If packets are dropped, lower the sample rate in Settings.")
                HelpStep("•", "Settings cannot be changed while streaming is active.")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun formatDataRate(bps: Int): String = when {
    bps >= 1_000_000 -> "${"%.1f".format(bps / 1_000_000f)} MB/s"
    bps >= 1_000     -> "${"%.1f".format(bps / 1_000f)} KB/s"
    else             -> "$bps B/s"
}

@Composable
private fun HelpSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        content()
    }
}

@Composable
private fun HelpPara(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.6)
}

@Composable
private fun HelpStep(number: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(number, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun HelpCode(text: String) {
    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text       = text,
            style      = MaterialTheme.typography.bodyMedium,
            color      = MaterialTheme.colorScheme.onBackground,
            modifier   = Modifier.padding(12.dp),
            lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.7
        )
    }
}
