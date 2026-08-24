package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioAccentLight
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface

@Composable
fun StudioSoundwaveLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val h1 by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 8f,
            targetValue = 18f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h1"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(8f) }
    }

    val h2 by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 16f,
            targetValue = 26f,
            animationSpec = infiniteRepeatable(
                animation = tween(750, delayMillis = 150, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h2"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(16f) }
    }

    val h3 by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 24f,
            targetValue = 32f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, delayMillis = 300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "h3"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(24f) }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Bar 1
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioAccentLight.copy(alpha = 0.6f))
            )
            // Bar 2
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioAccentLight.copy(alpha = 0.8f))
            )
            // Bar 3 (Center)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioAccent)
            )
            // Bar 4
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h2.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioAccentLight.copy(alpha = 0.8f))
            )
            // Bar 5
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h1.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioAccentLight.copy(alpha = 0.6f))
            )
        }
    }
}
