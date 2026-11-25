package com.soundsticker.studio.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soundsticker.studio.ui.theme.EmeraldGreen
import com.soundsticker.studio.ui.theme.WhatsAppSurfaceVariant
import kotlin.random.Random

@Composable
fun WaveformBar(
    isPlaying: Boolean,
    isRecording: Boolean,
    progressFraction: Float = 0f,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformPulse")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(WhatsAppSurfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val random = Random(42)
        val defaultHeights = List(barCount) { 0.2f + random.nextFloat() * 0.75f }

        for (i in 0 until barCount) {
            val barFraction = i.toFloat() / barCount
            val isPassed = barFraction <= progressFraction

            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + (i % 5) * 80, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barScale_$i"
            )

            val baseHeight = defaultHeights[i]
            val finalHeightFraction = when {
                isRecording -> (baseHeight * animatedScale).coerceIn(0.15f, 1f)
                isPlaying -> if (isPassed) (baseHeight * animatedScale).coerceIn(0.15f, 1f) else baseHeight
                else -> baseHeight
            }

            val barColor = when {
                isRecording -> Color(0xFFEF5350) // Red while recording
                isPassed -> EmeraldGreen        // Emerald for active playback
                else -> Color(0xFF8696A0)       // Muted gray
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((48 * finalHeightFraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
