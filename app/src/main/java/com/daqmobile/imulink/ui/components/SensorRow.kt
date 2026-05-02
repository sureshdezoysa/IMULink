package com.daqmobile.imulink.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqmobile.imulink.sensor.AxisData

@Composable
fun SensorRow(
    label: String,
    unit: String,
    data: AxisData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = unit, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(
            modifier  = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outline
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)
        ) {
            AxisValue(value = data.x, axis = "x", modifier = Modifier.weight(1f))
            AxisValue(value = data.y, axis = "y", modifier = Modifier.weight(1f))
            AxisValue(value = data.z, axis = "z", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AxisValue(value: Float, axis: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text       = formatAxisValue(value),
            fontSize   = 22.sp,
            fontFamily = MaterialTheme.typography.displaySmall.fontFamily,
            fontWeight = MaterialTheme.typography.displaySmall.fontWeight,
            color      = MaterialTheme.colorScheme.onBackground,
            maxLines   = 1,
            softWrap   = false,
            textAlign  = TextAlign.Start
        )
        Text(
            text     = axis,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
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
