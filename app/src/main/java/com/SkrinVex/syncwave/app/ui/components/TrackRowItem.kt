package com.SkrinVex.syncwave.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRowItem(
    track: Track,
    coverUrl: String,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    index: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onAddToQueue: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    isDownloaded: Boolean = false,
    onDownload: (() -> Unit)? = null,
    onDeleteDownloaded: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imageRequest = remember(coverUrl) {
        ImageRequest.Builder(context)
            .data(coverUrl)
            .size(128, 128)
            .crossfade(100)
            .build()
    }

    val backgroundColor = when {
        isSelected -> StudioAccent.copy(alpha = 0.14f)
        isCurrentTrack -> StudioHover
        else -> StudioSurface
    }

    val borderColor = when {
        isSelected -> StudioAccent.copy(alpha = 0.6f)
        isCurrentTrack -> StudioAccent.copy(alpha = 0.4f)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox OR Track Index / Equalizer Animation
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StudioAccent,
                        uncheckedColor = Zinc500,
                        checkmarkColor = Zinc100
                    ),
                    modifier = Modifier.size(20.dp)
                )
            } else if (isCurrentTrack) {
                StudioEqualizerAnimation(
                    isPlaying = isPlaying,
                    maxHeight = 13.dp,
                    barWidth = 2.5.dp,
                    color = StudioAccent
                )
            } else {
                Text(
                    text = "$index",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Zinc500
                )
            }
        }

        // Cover Art Thumbnail with playing overlay
        Box(
            modifier = Modifier
                .size(42.dp)
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

            if (isCurrentTrack) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    StudioEqualizerAnimation(
                        isPlaying = isPlaying,
                        maxHeight = 12.dp,
                        barWidth = 2.dp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title, Artist, Album
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                fontSize = 13.sp,
                fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isCurrentTrack) StudioAccent else Zinc100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = track.artist.ifBlank { "Unknown Artist" },
                    fontSize = 11.sp,
                    color = Zinc400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (track.album.isNotBlank()) {
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Zinc500
                    )
                    Text(
                        text = track.album,
                        fontSize = 11.sp,
                        color = Zinc500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Downloaded Offline Icon Indicator
        if (isDownloaded) {
            Icon(
                imageVector = Icons.Default.FileDownloadDone,
                contentDescription = "Скачано на устройство",
                tint = StudioEmerald,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Format Badge
        StudioBadge(
            text = track.format.uppercase(),
            backgroundColor = StudioElevated,
            textColor = Zinc400
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = track.formattedDuration,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Zinc400
        )

        // Options Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Опции",
                    tint = Zinc400,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (showMenu) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(StudioElevated)
                ) {
                    if (onAddToQueue != null) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                        contentDescription = null,
                                        tint = StudioAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Добавить в очередь", color = Zinc100, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onAddToQueue()
                            }
                        )
                    }

                    if (onPlayNext != null) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = null,
                                        tint = Zinc300,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Играть следующим", color = Zinc100, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onPlayNext()
                            }
                        )
                    }

                    // Download / Remove from device action
                    if (isDownloaded && onDeleteDownloaded != null) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = Zinc400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Удалить с устройства", color = Zinc300, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDeleteDownloaded()
                            }
                        )
                    } else if (!isDownloaded && onDownload != null) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = StudioAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Скачать на устройство", color = Zinc100, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDownload()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = StudioRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Удалить с сервера", color = StudioRed, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
