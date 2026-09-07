package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent

private val BAR_SPACING = 1.5.dp
private val IDLE_HEIGHTS = floatArrayOf(0.4f, 0.7f, 0.9f, 0.5f)
private val BOTTOM_ORIGIN = TransformOrigin(0.5f, 1f)

/**
 * Four bars pulsing while a track plays.
 *
 * The bars keep a fixed height and are scaled in the draw phase, so a running
 * animation never re-measures or re-lays-out the row that hosts it. When paused
 * no animation is created at all.
 */
@Composable
fun StudioEqualizerAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 14.dp,
    barWidth: Dp = 2.5.dp,
    color: Color = StudioAccent
) {
    if (!isPlaying) {
        StaticEqualizerBars(
            heights = IDLE_HEIGHTS,
            modifier = modifier,
            maxHeight = maxHeight,
            barWidth = barWidth,
            color = color
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "equalizer_bars")

    val h1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )

    val h2 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )

    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, delayMillis = 75),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    val h4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(BAR_SPACING),
        verticalAlignment = Alignment.Bottom
    ) {
        AnimatedBar({ h1 }, maxHeight, barWidth, color)
        AnimatedBar({ h2 }, maxHeight, barWidth, color)
        AnimatedBar({ h3 }, maxHeight, barWidth, color)
        AnimatedBar({ h4 }, maxHeight, barWidth, color)
    }
}

/**
 * The scale is read inside [graphicsLayer], which runs in the draw phase, so each
 * frame skips composition, measure and layout entirely.
 */
@Composable
private fun AnimatedBar(
    scale: () -> Float,
    maxHeight: Dp,
    barWidth: Dp,
    color: Color
) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .height(maxHeight)
            .graphicsLayer {
                scaleY = scale().coerceIn(0.05f, 1f)
                transformOrigin = BOTTOM_ORIGIN
            }
            .clip(RoundedCornerShape(1.dp))
            .background(color)
    )
}

@Composable
private fun StaticEqualizerBars(
    heights: FloatArray,
    modifier: Modifier,
    maxHeight: Dp,
    barWidth: Dp,
    color: Color
) {
    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(BAR_SPACING),
        verticalAlignment = Alignment.Bottom
    ) {
        for (fraction in heights) {
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxHeight * fraction)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}
