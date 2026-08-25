package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioHover
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500
import com.SkrinVex.syncwave.app.ui.theme.Zinc950
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerBottomSheet(
    playerState: PlayerState,
    coverModel: Any?,
    getTrackCoverModel: (String) -> Any,
    onDismiss: () -> Unit,

    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSelectQueueTrack: ((Int) -> Unit)? = null,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    onReshuffleQueue: (() -> Unit)? = null,
    onClearQueue: (() -> Unit)? = null,
    isDownloaded: Boolean = false,
    isDownloading: Boolean = false,
    downloadProgress: Int = 0,
    onDownloadTrack: (() -> Unit)? = null,
    onDeleteDownloadedTrack: (() -> Unit)? = null
) {
    val track = playerState.currentTrack ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var isQueueVisible by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showDeleteDownloadedConfirm by remember { mutableStateOf(false) }

    // Double-tap feedback state
    var showRewindFeedback by remember { mutableStateOf(false) }
    var showForwardFeedback by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentMs = if (isSeeking) seekPosition.toLong() else playerState.currentPositionMs
    val totalMs = if (playerState.durationMs > 0) playerState.durationMs else (track.duration * 1000L).coerceAtLeast(1L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioBg,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudioBg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 8.dp, start = 18.dp, end = 18.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dismiss Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
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

                // Center Title
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
                        .size(38.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            if (isQueueVisible) {
                // Queue View
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Queue Action Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
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
                                        model = coverModel,
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
                            val qCoverModel = getTrackCoverModel(qTrack.id)

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

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StudioElevated)
                                            .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = qCoverModel,
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
                // Main Artwork View with Double-Tap Gestures
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
                            .shadow(28.dp, RoundedCornerShape(22.dp), spotColor = StudioAccent.copy(alpha = 0.45f))
                            .clip(RoundedCornerShape(22.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(22.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { offset ->
                                        if (offset.x < size.width / 2) {
                                            onRewind10()
                                            scope.launch {
                                                showRewindFeedback = true
                                                delay(650)
                                                showRewindFeedback = false
                                            }
                                        } else {
                                            onForward10()
                                            scope.launch {
                                                showForwardFeedback = true
                                                delay(650)
                                                showForwardFeedback = false
                                            }
                                        }
                                    },
                                    onTap = {
                                        onPlayPause()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = coverModel,
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )


                        // Left Rewind Double-Tap Feedback
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showRewindFeedback,
                            enter = fadeIn() + scaleIn(initialScale = 0.7f),
                            exit = fadeOut() + scaleOut(targetScale = 1.2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "-10s",
                                        tint = Zinc100,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "-10с",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Zinc100
                                    )
                                }
                            }
                        }

                        // Right Forward Double-Tap Feedback
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showForwardFeedback,
                            enter = fadeIn() + scaleIn(initialScale = 0.7f),
                            exit = fadeOut() + scaleOut(targetScale = 1.2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "+10s",
                                        tint = Zinc100,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "+10с",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Zinc100
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        modifier = Modifier.padding(top = 3.dp),
                        textAlign = TextAlign.Center,
                        enableMarquee = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Info, Speed & Download Auxiliary Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio Format & Quality Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StudioBadge(
                            text = track.format.uppercase(),
                            backgroundColor = StudioEmerald.copy(alpha = 0.15f),
                            textColor = StudioEmerald
                        )

                        if (track.bitrate > 0) {
                            StudioBadge(
                                text = "${track.bitrate} kbps",
                                backgroundColor = StudioElevated,
                                textColor = Zinc400
                            )
                        }
                    }

                    // Speed Pill & Download Action Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Download Pill Button
                        if (isDownloaded) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, StudioEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { showDeleteDownloadedConfirm = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownloadDone,
                                    contentDescription = "Скачано",
                                    tint = StudioEmerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Скачано",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioEmerald
                                )
                            }
                        } else if (isDownloading) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioAccent.copy(alpha = 0.15f))
                                    .border(1.dp, StudioAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    color = StudioAccent,
                                    strokeWidth = 1.5.dp
                                )
                                Text(
                                    text = "$downloadProgress%",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioAccent
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioElevated)
                                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                                    .clickable { onDownloadTrack?.invoke() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Скачать",
                                    tint = Zinc300,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Скачать",
                                    fontSize = 11.sp,
                                    color = Zinc300
                                )
                            }
                        }

                        // Speed Pill Selector Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioElevated)
                                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable { showSpeedDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Скорость",
                            tint = if (playerState.playbackSpeed != 1.0f) StudioAccent else Zinc400,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${playerState.playbackSpeed}x",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (playerState.playbackSpeed != 1.0f) StudioAccent else Zinc300
                        )
                    }
                }
            }

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                // Full Playback Control Buttons Row with Rewind 10 / Forward 10
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button
                    IconButton(onClick = onToggleShuffle, modifier = Modifier.size(38.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Перемешать",
                                tint = if (playerState.isShuffle) StudioAccent else Zinc500,
                                modifier = Modifier.size(20.dp)
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

                    // Rewind 10 Seconds Button
                    IconButton(onClick = onRewind10, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Назад на 10 сек",
                            tint = Zinc300,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous Track Button
                    IconButton(onClick = onPrevious, modifier = Modifier.size(42.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Предыдущий",
                            tint = Zinc100,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Elevated Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(14.dp, CircleShape, spotColor = StudioAccent.copy(alpha = 0.55f))
                            .clip(CircleShape)
                            .background(Zinc100)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = Zinc950,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Пауза" else "Играть",
                                tint = Zinc950,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Next Track Button
                    IconButton(onClick = onNext, modifier = Modifier.size(42.dp)) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Следующий",
                            tint = Zinc100,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Forward 10 Seconds Button
                    IconButton(onClick = onForward10, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Вперед на 10 сек",
                            tint = Zinc300,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Repeat Button
                    IconButton(onClick = onCycleRepeat, modifier = Modifier.size(38.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (playerState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "Повтор",
                                tint = if (playerState.repeatMode != RepeatMode.OFF) StudioAccent else Zinc500,
                                modifier = Modifier.size(20.dp)
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

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Speed Selection Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Скорость воспроизведения",
                        color = Zinc100,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    speeds.forEach { speed ->
                        val isSelected = playerState.playbackSpeed == speed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) StudioAccent.copy(alpha = 0.2f) else StudioElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) StudioAccent.copy(alpha = 0.6f) else StudioBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSetPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (speed == 1.0f) "1.0x (Обычная)" else "${speed}x",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) StudioAccent else Zinc100
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StudioAccent)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Готово", color = StudioAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // Confirmation Dialog for removing downloaded track from device
    if (showDeleteDownloadedConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDownloadedConfirm = false },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Удаление с устройства", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Трек '${track.title}' скачан на это устройство. Вы действительно хотите удалить его из памяти устройства?",
                    color = Zinc400,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDownloadedConfirm = false
                    onDeleteDownloadedTrack?.invoke()
                }) {
                    Text("Удалить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDownloadedConfirm = false }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
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
