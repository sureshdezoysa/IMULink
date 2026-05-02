package com.daqmobile.imulink.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Help & Guide", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = MaterialTheme.colorScheme.background,
                    titleContentColor     = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize().padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            HelpSection("WHAT IS IMULINK?") {
                HelpPara("IMULink streams raw IMU sensor data from your Android phone to any computer on the same Wi-Fi network using UDP. You can record or process the data in Python, MATLAB, C#, or any language that supports UDP sockets.")
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
                HelpCode("timestamp_ms,ax,ay,az,gx,gy,gz,mx,my,mz\n\ntimestamp_ms  — Unix time in milliseconds\nax, ay, az    — Accelerometer  (m/s²)\ngx, gy, gz    — Gyroscope       (rad/s)\nmx, my, mz    — Magnetometer    (µT)")
                HelpPara("Example:")
                HelpCode("1716300000123,+0.279,+5.299,+8.247,-0.046,-0.022,-0.010,-12.78,-38.24,+28.23")
            }

            HelpSection("PYTHON RECEIVER EXAMPLE") {
                HelpCode("import socket\n\nUDP_IP   = \"0.0.0.0\"\nUDP_PORT = 5005\n\nsock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)\nsock.bind((UDP_IP, UDP_PORT))\n\nprint(\"Waiting for IMULink data...\")\nwhile True:\n    data, addr = sock.recvfrom(1024)\n    values = data.decode().strip().split(',')\n    ts = int(values[0])\n    ax, ay, az = float(values[1]), float(values[2]), float(values[3])\n    gx, gy, gz = float(values[4]), float(values[5]), float(values[6])\n    mx, my, mz = float(values[7]), float(values[8]), float(values[9])\n    print(f\"t={ts} acc=({ax:.3f},{ay:.3f},{az:.3f})\")")
            }

            HelpSection("FREE vs PRO") {
                HelpPara("Free tier: streaming stops after 5 minutes. Sample rate capped at 50 Hz.")
                HelpPara("Pro (one-time purchase): unlimited streaming time and top-speed sensor rate.")
            }

            HelpSection("TIPS") {
                HelpStep("•", "Use a 5 GHz Wi-Fi network for lowest latency.")
                HelpStep("•", "Keep the phone plugged in during long recording sessions.")
                HelpStep("•", "The ⚡ button engages top-speed mode (Pro only).")
                HelpStep("•", "If packets are dropped, lower the sample rate in Settings.")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
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
    Surface(shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(12.dp),
            lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.7)
    }
}
