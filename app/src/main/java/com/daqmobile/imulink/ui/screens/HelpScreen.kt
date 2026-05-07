package com.daqmobile.imulink.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.daqmobile.imulink.R
import com.daqmobile.imulink.ui.MainViewModel
import com.daqmobile.imulink.ui.StreamState
import com.daqmobile.imulink.ui.screens.StreamingGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val streamState by viewModel.streamState.collectAsState()
    val settings    by viewModel.settings.collectAsState()
    val dataRateBps by viewModel.dataRateBps.collectAsState()
    val isStreaming  = streamState != StreamState.IDLE
    val context        = LocalContext.current

    // Build the full shareable text from all string resources
    val shareText = buildShareText(context)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.help_title),
                            style = MaterialTheme.typography.headlineLarge)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        // Share button
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "IMULink — Help & Guide")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Share IMULink Guide"))
                        }) {
                            Icon(Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.help_share),
                                tint = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor             = MaterialTheme.colorScheme.background,
                        titleContentColor          = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                // Streaming banner
                AnimatedVisibility(
                    visible = isStreaming,
                    enter   = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit    = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ) {
                    val rateText = if (dataRateBps > 0) "  ·  ${formatRate(dataRateBps)}" else ""
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
                            Text("● ", style = MaterialTheme.typography.bodyMedium,
                                color = StreamingGreen)
                            Text(
                                "Streaming to: ${settings.receiverIp}:${settings.udpPort}$rateText",
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

            // ── WHAT IS IMULINK ───────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_what)) {
                HelpPara(stringResource(R.string.help_what_body))
            }

            // ── QUICK START ───────────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_quickstart)) {
                HelpStep("1", stringResource(R.string.help_step_1))
                HelpStep("2", stringResource(R.string.help_step_2))
                HelpStep("3", stringResource(R.string.help_step_3))
                HelpStep("4", stringResource(R.string.help_step_4))
                HelpStep("5", stringResource(R.string.help_step_5))
            }

            // ── FINDING YOUR IP ───────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_find_ip)) {
                HelpPara(stringResource(R.string.help_find_ip_mac))
                HelpPara(stringResource(R.string.help_find_ip_win))
                HelpPara(stringResource(R.string.help_find_ip_phone))
            }

            // ── COORDINATE SYSTEM ─────────────────────────────────────────
            HelpSection(stringResource(R.string.help_coordinate_system)) {
                HelpPara(stringResource(R.string.help_coordinate_body))
            }

            // ── COMPLETE DATA FORMAT ──────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_data_format)) {
                HelpPara(stringResource(R.string.help_full_format_body))
                CopyableCode(
                    context = context,
                    text    =
                        "CSV field order (enabled sensors only, no placeholders):\n\n" +
                        "timestamp_ms\n" +
                        "  ax, ay, az          — Accelerometer  (m/s²)\n" +
                        "  gx, gy, gz          — Gyroscope       (rad/s)\n" +
                        "  mx, my, mz          — Magnetometer    (µT)\n" +
                        "  gravx, gravy, gravz — Gravity          (m/s²)\n" +
                        "  lax, lay, laz       — Linear Accel     (m/s²)\n" +
                        "  rotx, roty, rotz, rotw — Rotation (quaternion)\n\n" +
                        "Example (all sensors enabled):\n" +
                        "1746000000000,0.12,-0.03,9.81," +
                        "0.001,-0.002,0.000," +
                        "-14.2,38.1,-5.3," +
                        "0.01,-0.02,9.80," +
                        "0.11,-0.01,0.01," +
                        "-0.004,0.001,0.998,0.063"
                )
            }

            // ── PYTHON RECEIVER ───────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_python)) {
                CopyableCode(
                    context = context,
                    text    =
                        "import socket\n\n" +
                        "HOST = '0.0.0.0'   # listen on all interfaces\n" +
                        "PORT = 5005\n\n" +
                        "sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)\n" +
                        "sock.bind((HOST, PORT))\n" +
                        "print(f'Listening on {HOST}:{PORT} ...')\n\n" +
                        "while True:\n" +
                        "    data, addr = sock.recvfrom(4096)\n" +
                        "    fields = data.decode().strip().split(',')\n" +
                        "    ts   = int(fields[0])\n" +
                        "    ax, ay, az = float(fields[1]), float(fields[2]), float(fields[3])\n" +
                        "    gx, gy, gz = float(fields[4]), float(fields[5]), float(fields[6])\n" +
                        "    print(f'[{ts}] acc=({ax:.3f},{ay:.3f},{az:.3f})  " +
                        "gyro=({gx:.4f},{gy:.4f},{gz:.4f})')"
                )
            }

            // ── MATLAB RECEIVER ───────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_matlab)) {
                HelpPara(stringResource(R.string.help_matlab_desc))
                CopyableCode(
                    context = context,
                    text    =
                        "u = udpport('datagram', 'LocalPort', 5005);\n" +
                        "disp('Waiting for IMULink data...');\n" +
                        "while true\n" +
                        "    if u.NumDatagramsAvailable > 0\n" +
                        "        dg  = read(u, 1, 'string');\n" +
                        "        v   = str2double(strsplit(dg.Data{1}, ','));\n" +
                        "        ts  = v(1);  ax = v(2);  ay = v(3);  az = v(4);\n" +
                        "        fprintf('t=%d  ax=%.3f  ay=%.3f  az=%.3f\\n'," +
                        "ts, ax, ay, az);\n" +
                        "    end\n" +
                        "    pause(0.001);\n" +
                        "end"
                )
            }

            // ── C# / UNITY RECEIVER ───────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_csharp)) {
                HelpPara(stringResource(R.string.help_csharp_desc))
                CopyableCode(
                    context = context,
                    text    =
                        "using System.Net;\n" +
                        "using System.Net.Sockets;\n" +
                        "using System.Text;\n\n" +
                        "UdpClient udp = new UdpClient(5005);\n" +
                        "IPEndPoint ep = new IPEndPoint(IPAddress.Any, 5005);\n\n" +
                        "while (true) {\n" +
                        "    byte[] data  = udp.Receive(ref ep);\n" +
                        "    string line  = Encoding.UTF8.GetString(data).Trim();\n" +
                        "    string[] v   = line.Split(',');\n" +
                        "    long   ts    = long.Parse(v[0]);\n" +
                        "    float  ax    = float.Parse(v[1]);\n" +
                        "    float  ay    = float.Parse(v[2]);\n" +
                        "    float  az    = float.Parse(v[3]);\n" +
                        "    Debug.Log(\$\"t={ts} acc=({ax:F3},{ay:F3},{az:F3})\");\n" +
                        "}"
                )
            }

            // ── USING WITH AI TOOLS ───────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_ai_prompt)) {
                HelpPara(stringResource(R.string.help_ai_body))
            }

            // ── SOURCE CODE / GITHUB ──────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_github)) {
                HelpPara(stringResource(R.string.help_github_body))
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse(context.getString(R.string.help_github_url)))
                        context.startActivity(intent)
                    },
                    shape  = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.help_github_button),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // ── TIPS ──────────────────────────────────────────────────────
            HelpSection(stringResource(R.string.help_section_tips)) {
                HelpStep("•", stringResource(R.string.help_tip_1))
                HelpStep("•", stringResource(R.string.help_tip_2))
                HelpStep("•", stringResource(R.string.help_tip_4))
                HelpStep("•", stringResource(R.string.help_tip_5))
                HelpStep("•", stringResource(R.string.help_tip_3_updated))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Copyable code block ───────────────────────────────────────────────────────

@Composable
private fun CopyableCode(context: Context, text: String) {
    val scope       = rememberCoroutineScope()
    var justCopied  by remember { mutableStateOf(false) }
    val copyLabel   = stringResource(R.string.help_copy)
    val copiedLabel = stringResource(R.string.help_copied)

    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Copy button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 8.dp, top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE)
                                as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("IMULink code", text))
                        justCopied = true
                        scope.launch {
                            delay(2000)
                            justCopied = false
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = copyLabel,
                        tint     = if (justCopied) StreamingGreen
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = if (justCopied) copiedLabel else copyLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (justCopied) StreamingGreen
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Code text
            Text(
                text       = text,
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.padding(
                    start = 12.dp, end = 12.dp, bottom = 12.dp),
                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.7
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildShareText(context: Context): String = with(context) {
    buildString {
        appendLine("IMULink — Help & Guide")
        appendLine("=".repeat(40))
        appendLine()
        appendLine(getString(R.string.help_section_what))
        appendLine(getString(R.string.help_what_body))
        appendLine()
        appendLine(getString(R.string.help_section_quickstart))
        for (i in 1..5) {
            val id = context.resources.getIdentifier(
                "help_step_$i", "string", context.packageName)
            if (id != 0) appendLine("$i. ${getString(id)}")
        }
        appendLine()
        appendLine(getString(R.string.help_coordinate_system))
        appendLine(getString(R.string.help_coordinate_body))
        appendLine()
        appendLine(getString(R.string.help_section_data_format))
        appendLine(getString(R.string.help_full_format_body))
        appendLine()
        appendLine("CSV field order (all sensors enabled):")
        appendLine("timestamp_ms, ax,ay,az, gx,gy,gz, mx,my,mz, gravx,gravy,gravz, lax,lay,laz, rotx,roty,rotz,rotw")
        appendLine()
        appendLine(getString(R.string.help_section_python))
        appendLine()
        appendLine(getString(R.string.help_section_matlab))
        appendLine(getString(R.string.help_matlab_desc))
        appendLine()
        appendLine(getString(R.string.help_section_csharp))
        appendLine(getString(R.string.help_csharp_desc))
        appendLine()
        appendLine(getString(R.string.help_section_ai_prompt))
        appendLine(getString(R.string.help_ai_body))
        appendLine()

        appendLine(getString(R.string.help_section_tips))
        appendLine("• ${getString(R.string.help_tip_1)}")
        appendLine("• ${getString(R.string.help_tip_2)}")
        appendLine("• ${getString(R.string.help_tip_4)}")
        appendLine("• ${getString(R.string.help_tip_5)}")
        appendLine("• ${getString(R.string.help_tip_3_updated)}")
    }
}

private fun formatRate(bps: Int): String = when {
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
    Text(text,
        style      = MaterialTheme.typography.bodyMedium,
        color      = MaterialTheme.colorScheme.onBackground,
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
