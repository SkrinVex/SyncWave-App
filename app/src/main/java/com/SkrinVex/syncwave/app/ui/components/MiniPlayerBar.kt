package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.SkrinVex.syncwave.app.player.PlayerState
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MiniPlayerBar(
    playerState: PlayerState,
    coverUrl: String,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack ?: return
    val progressFraction = if (playerState.durationMs > 0) {
        (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val context = LocalContext.current
    val imageRequest = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .size(128, 128)
            .crossfade(100)
            .build()
    }

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 55.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .graphicsLayer {
                val progress = (offsetY.value / (dismissThresholdPx * 2.2f)).coerceIn(0f, 1f)
                alpha = (1f - (progress * 0.75f)).coerceIn(0f, 1f)
                val scale = 1f - (progress * 0.08f)
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(StudioSurface)
            .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value > dismissThresholdPx) {
                                // Animate downward and dismiss
                                offsetY.animateTo(
                                    targetValue = dismissThresholdPx * 2.8f,
                                    animationSpec = tween(durationMillis = 180)
                                )
                                onDismiss()
                                offsetY.snapTo(0f)
                            } else if (offsetY.value < -dismissThresholdPx * 0.6f) {
                                // Swiped up -> expand full player
                                offsetY.animateTo(0f)
                                onExpand()
                            } else {
                                // Snap back smoothly with spring
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offsetY.animateTo(0f)
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val current = offsetY.value
                            val next = current + dragAmount
                            if (next < 0f) {
                                offsetY.snapTo(next * 0.35f) // Resistance when pulling up
                            } else {
                                offsetY.snapTo(next) // Direct 1:1 finger tracking when swiping down
                            }
                        }
                    }
                )
            }
            .clickable(onClick = {
                if (offsetY.value == 0f) {
                    onExpand()
                }
            })
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Artwork & Marquee Titles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Artwork Thumbnail with playing equalizer overlay
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioElevated)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (playerState.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                StudioEqualizerAnimation(
                                    isPlaying = true,
                                    maxHeight = 12.dp,
                                    barWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Marquee Title and Artist
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        MarqueeText(
                            text = track.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100,
                            enableMarquee = true
                        )
                        MarqueeText(
                            text = track.artist.ifBlank { "Unknown Artist" },
                            fontSize = 11.sp,
                            color = Zinc400,
                            modifier = Modifier.padding(top = 1.dp),
                            enableMarquee = true
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Playback Controls (Play/Pause, Next)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Play/Pause Button
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = StudioAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Пауза" else "Играть",
                                tint = Zinc100,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Следующий трек",
                            tint = Zinc400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Slim Bottom Progress Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(StudioElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(2.dp)
                        .background(StudioAccent)
                )
            }
        }
    }
}
