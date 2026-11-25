package com.soundsticker.studio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppSurfaceVariant
import com.soundsticker.studio.ui.theme.WhatsAppTextPrimary
import com.soundsticker.studio.ui.theme.WhatsAppTextSecondary
import java.util.Locale

@Composable
fun RangeSliderTrimmer(
    totalDurationMs: Long,
    startMs: Long,
    endMs: Long,
    maxAllowedDurationMs: Long,
    onRangeChange: (start: Long, end: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (totalDurationMs / 1000f).coerceAtLeast(1f)
    val startSeconds = (startMs / 1000f).coerceIn(0f, totalSeconds)
    val endSeconds = (endMs / 1000f).coerceIn(startSeconds, totalSeconds)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inicio: ${formatTime(startMs)}",
                color = WhatsAppTextSecondary,
                fontSize = 13.sp
            )
            val selectedDurationMs = (endMs - startMs).coerceAtLeast(0)
            val isExceeding = selectedDurationMs > maxAllowedDurationMs

            Text(
                text = "Duración: ${formatTime(selectedDurationMs)} / Máx ${maxAllowedDurationMs / 1000}s",
                color = if (isExceeding) com.soundsticker.studio.ui.theme.ErrorRed else EmeraldGreen,
                fontSize = 13.sp
            )

            Text(
                text = "Fin: ${formatTime(endMs)}",
                color = WhatsAppTextSecondary,
                fontSize = 13.sp
            )
        }

        RangeSlider(
            value = startSeconds..endSeconds,
            onValueChange = { range ->
                val newStartMs = (range.start * 1000).toLong()
                var newEndMs = (range.endInclusive * 1000).toLong()

                // Clamp to max allowed duration if needed
                if (newEndMs - newStartMs > maxAllowedDurationMs) {
                    newEndMs = newStartMs + maxAllowedDurationMs
                }
                onRangeChange(newStartMs, newEndMs)
            },
            valueRange = 0f..totalSeconds,
            colors = SliderDefaults.colors(
                thumbColor = EmeraldGreen,
                activeTrackColor = EmeraldGreen,
                inactiveTrackColor = WhatsAppSurfaceVariant
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    val millis = (ms % 1000) / 100
    return String.format(Locale.US, "%02d:%02d.%01d", minutes, seconds, millis)
}
