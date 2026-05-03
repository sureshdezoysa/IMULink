package com.daqmobile.imulink.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqmobile.imulink.sensor.AxisData
import com.daqmobile.imulink.ui.theme.StreamGreen

@Composable
fun SensorRow(
    label: String,
    unit: String,
    data: AxisData,
    isStreaming: Boolean = false,
    dimmed: Boolean      = false,
    modifier: Modifier   = Modifier
) {
    val valueColor: Color = when {
        isStreaming -> StreamGreen
        dimmed      -> Color(0xFF666666)   // lighter — was 0xFF444444
        else        -> Color.White
    }
    val labelColor: Color = when {
        dimmed -> Color(0xFF555555)        // lighter — was 0xFF3A3A3A
        else   -> Color(0xFF888888)
    }
    val dividerColor: Color = when {
        isStreaming -> StreamGreen.copy(alpha = 0.4f)
        dimmed      -> Color(0xFF3A3A3A)   // lighter — was 0xFF2A2A2A
        else        -> Color(0xFF3A3A3A)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = labelColor)
            Text(text = unit,  style = MaterialTheme.typography.bodyMedium, color = labelColor)
        }
        HorizontalDivider(
            modifier  = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp,
            color     = dividerColor
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AxisValue(data.x, "x", TextAlign.Start,  valueColor, Modifier.weight(1f))
            AxisValue(data.y, "y", TextAlign.Center, valueColor, Modifier.weight(1f))
            AxisValue(data.z, "z", TextAlign.End,    valueColor, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AxisValue(
    value: Float, axis: String,
    textAlign: TextAlign, color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = when (textAlign) {
            TextAlign.Center -> Arrangement.Center
            TextAlign.End    -> Arrangement.End
            else             -> Arrangement.Start
        }
    ) {
        Text(
            text       = formatAxisValue(value),
            fontSize   = 22.sp,
            fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
            fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
            color      = color,
            maxLines   = 1,
            softWrap   = false
        )
        Text(
            text     = axis,
            style    = MaterialTheme.typography.bodySmall,
            color    = color.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 1.dp, bottom = 3.dp)
        )
    }
}

private fun formatAxisValue(v: Float): String {
    val sign = if (v >= 0f) "+" else "-"
    val abs  = Math.abs(v)
    return when {
        abs < 10f   -> "$sign${"%.3f".format(abs)}"
        abs < 100f  -> "$sign${"%.2f".format(abs)}"
        abs < 1000f -> "$sign${"%.1f".format(abs)}"
        else        -> "$sign${"%.0f".format(abs)}"
    }
}
