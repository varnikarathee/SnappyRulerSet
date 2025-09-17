package com.example.snappyrulerset.ui

import android.graphics.PointF
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.snappyrulerset.geom.distance
import com.example.snappyrulerset.model.CanvasState

@Composable
fun PrecisionHUD(
    state: CanvasState,
    currentStart: PointF?,
    currentEnd: PointF?,
    isSnapEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val lengthText = if (currentStart != null && currentEnd != null) {
        val lenPx = distance(currentStart, currentEnd)
        val lenMm = lenPx / 4f // placeholder scale; replace with real dpi conversion
        String.format("%.1f mm", lenMm)
    } else "--"

    val angleText = "--" // Angle compute could be added when rays known

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC000000))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "L: $lengthText   A: $angleText", color = Color.White, style = MaterialTheme.typography.bodyMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { state.undo() }, enabled = true) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Undo", tint = Color.White)
            }
            IconButton(onClick = { state.redo() }, enabled = true) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Redo", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isSnapEnabled.value) "Snap: ON" else "Snap: OFF",
                color = if (isSnapEnabled.value) Color(0xFF81C784) else Color(0xFFE57373),
                modifier = Modifier
                    .clickable { isSnapEnabled.value = !isSnapEnabled.value }
                    .padding(6.dp)
            )
        }
    }
}

