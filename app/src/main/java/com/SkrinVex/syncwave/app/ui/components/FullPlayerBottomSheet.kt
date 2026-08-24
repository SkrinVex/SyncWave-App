package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.SkrinVex.syncwave.app.player.PlayerState
import com.SkrinVex.syncwave.app.player.RepeatMode
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500
import com.SkrinVex.syncwave.app.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerBottomSheet(
    playerState: PlayerState,
    coverUrl: String,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSelectQueueTrack: ((Int) -> Unit)? = null
) {
    val track = playerState.currentTrack ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var isQueueVisible by remember { mutableStateOf(false) }

    val currentMs = if (isSeeking) seekPosition.toLong() else playerState.currentPositionMs
    val totalMs = if (playerState.durationMs > 0) playerState.durationMs else (track.duration * 1000L).coerceAtLeast(1L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioBg,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Скрыть плеер",
                        tint = Zinc400,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isQueueVisible) "Очередь воспроизведения" else "Сейчас играет",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc400
                    )
                    if (playerState.queue.isNotEmpty()) {
                        Text(
                            text = "Трек ${playerState.currentIndex + 1} из ${playerState.queue.size}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StudioAccent
                        )
                    }
                }

                // Queue Toggle Button
                IconButton(
                    onClick = { isQueueVisible = !isQueueVisible },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isQueueVisible) StudioAccent.copy(alpha = 0.2f) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Очередь",
                        tint = if (isQueueVisible) StudioAccent else Zinc400,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isQueueVisible) {
                // Queue List View
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        text = "Далее в очереди (${playerState.queue.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc300,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(playerState.queue, key = { index, t -> "${t.id}_$index" }) { idx, qTrack ->
                            val isCurrent = idx == playerState.currentIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) StudioElevated else StudioSurface)
                                    .border(1.dp, if (isCurrent) StudioAccent.copy(alpha = 0.5f) else StudioBorder, RoundedCornerShape(10.dp))
                                    .clickable { onSelectQueueTrack?.invoke(idx) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.width(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCurrent) {
                                            StudioEqualizerAnimation(
                                                isPlaying = playerState.isPlaying,
                                                maxHeight = 12.dp,
                                                barWidth = 2.dp,
                                                color = StudioAccent
                                            )
                                        } else {
                                            Text(
                                                text = "${idx + 1}",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Zinc500
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = qTrack.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isCurrent) StudioAccent else Zinc100,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = qTrack.artist.ifBlank { "Unknown Artist" },
                                            fontSize = 10.sp,
                                            color = Zinc400,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = qTrack.formattedDuration,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc500
                                )
                            }
                        }
                    }
                }
            } else {
                // Main Artwork View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .aspectRatio(1f)
                            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = StudioAccent.copy(alpha = 0.35f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Artist with Marquee Text
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MarqueeText(
                        text = track.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Zinc100,
                        textAlign = TextAlign.Center,
                        enableMarquee = true
                    )
                    MarqueeText(
                        text = track.artist.ifBlank { "Unknown Artist" },
                        fontSize = 13.sp,
                        color = Zinc400,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                        enableMarquee = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seek Scrubber Row & Timers
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentMs.toFloat().coerceIn(0f, totalMs.toFloat()),
                        onValueChange = {
                            isSeeking = true
                            seekPosition = it
                        },
                        onValueChangeFinished = {
                            onSeek(seekPosition.toLong())
                            isSeeking = false
                        },
                        valueRange = 0f..totalMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Zinc100,
                            activeTrackColor = StudioAccent,
                            inactiveTrackColor = StudioElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatMs(currentMs),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc400
                        )
                        Text(
                            text = formatMs(totalMs),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Zinc400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Playback Control Buttons (Shuffle, Prev, Play/Pause, Next, Repeat)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = onToggleShuffle) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Перемешать",
                                tint = if (playerState.isShuffle) StudioAccent else Zinc500,
                                modifier = Modifier.size(22.dp)
                            )
                            if (playerState.isShuffle) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(StudioAccent)
                                )
                            }
                        }
                    }

                    // Previous
                    IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Предыдущий",
                            tint = Zinc100,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Main Big Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, CircleShape, spotColor = StudioAccent.copy(alpha = 0.5f))
                            .clip(CircleShape)
                            .background(Zinc100)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Zinc950,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Пауза" else "Играть",
                                tint = Zinc950,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Next
                    IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Следующий",
                            tint = Zinc100,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Repeat Mode
                    IconButton(onClick = onCycleRepeat) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Повтор",
                                tint = if (playerState.repeatMode != RepeatMode.OFF) StudioAccent else Zinc500,
                                modifier = Modifier.size(22.dp)
                            )
                            if (playerState.repeatMode != RepeatMode.OFF) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(StudioAccent)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0L)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format("%d:%02d", mins, secs)
}
