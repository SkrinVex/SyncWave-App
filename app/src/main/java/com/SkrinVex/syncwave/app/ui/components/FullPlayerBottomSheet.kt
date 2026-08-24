package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.SkrinVex.syncwave.app.ui.theme.StudioHover
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
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
    getTrackCoverUrl: (String) -> String,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSelectQueueTrack: ((Int) -> Unit)? = null,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    onReshuffleQueue: (() -> Unit)? = null,
    onClearQueue: (() -> Unit)? = null
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
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = null,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar with Top Inset Spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dismiss Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StudioElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Свернуть плеер",
                        tint = Zinc100,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isQueueVisible) "Очередь воспроизведения" else "Сейчас играет",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100
                    )
                    if (playerState.queue.isNotEmpty()) {
                        Text(
                            text = "Трек ${playerState.currentIndex + 1} из ${playerState.queue.size}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StudioAccent
                        )
                    }
                }

                // Queue Toggle Button
                IconButton(
                    onClick = { isQueueVisible = !isQueueVisible },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isQueueVisible) StudioAccent.copy(alpha = 0.25f) else StudioElevated)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Очередь",
                        tint = if (isQueueVisible) StudioAccent else Zinc100,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isQueueVisible) {
                // Queue Content View (Matching Web QueueDrawer)
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Queue Action Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Треков:",
                                fontSize = 12.sp,
                                color = Zinc400
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioElevated)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${playerState.queue.size}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioAccent
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Reshuffle Queue Button (Like Web)
                            if (playerState.queue.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioAccent.copy(alpha = 0.15f))
                                        .border(1.dp, StudioAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable { onReshuffleQueue?.invoke() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Перемешать",
                                        tint = StudioAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Перемешать",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StudioAccent
                                    )
                                }
                            }

                            // Clear Queue Button
                            if (playerState.queue.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioElevated)
                                        .clickable { onClearQueue?.invoke() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Очистить",
                                        fontSize = 11.sp,
                                        color = Zinc400
                                    )
                                }
                            }
                        }
                    }

                    // Now Playing Mini Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = track.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (playerState.isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.45f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            StudioEqualizerAnimation(
                                                isPlaying = true,
                                                maxHeight = 11.dp,
                                                barWidth = 2.dp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "СЕЙЧАС ИГРАЕТ",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioAccent
                                    )
                                    Text(
                                        text = track.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Zinc100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Text(
                                text = track.formattedDuration,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Zinc400,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Queue Track List
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(playerState.queue, key = { index, t -> "${t.id}_$index" }) { idx, qTrack ->
                            val isCurrent = idx == playerState.currentIndex
                            val qCoverUrl = getTrackCoverUrl(qTrack.id)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) StudioHover else StudioSurface)
                                    .border(
                                        1.dp,
                                        if (isCurrent) StudioAccent.copy(alpha = 0.5f) else StudioBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectQueueTrack?.invoke(idx) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Index
                                    Box(
                                        modifier = Modifier.width(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCurrent) {
                                            StudioEqualizerAnimation(
                                                isPlaying = playerState.isPlaying,
                                                maxHeight = 11.dp,
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

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Thumbnail Cover Art in Queue
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StudioElevated)
                                            .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = qCoverUrl,
                                            contentDescription = qTrack.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
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

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = qTrack.formattedDuration,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Zinc500
                                    )

                                    // Remove from Queue Button
                                    IconButton(
                                        onClick = { onRemoveFromQueue?.invoke(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Удалить из очереди",
                                            tint = Zinc500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
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
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
                            .shadow(28.dp, RoundedCornerShape(22.dp), spotColor = StudioAccent.copy(alpha = 0.45f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(22.dp)),
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

                Spacer(modifier = Modifier.height(18.dp))

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

                // Custom Studio Audio Scrubber with Buffering Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    StudioAudioScrubber(
                        currentPositionMs = currentMs,
                        bufferedPositionMs = playerState.bufferedPositionMs,
                        durationMs = totalMs,
                        onSeek = onSeek,
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

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0L)
    val h = totalSecs / 3600
    val m = (totalSecs % 3600) / 60
    val s = totalSecs % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}
