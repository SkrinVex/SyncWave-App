package com.SkrinVex.syncwave.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import com.SkrinVex.syncwave.app.domain.model.DownloadedTrack
import com.SkrinVex.syncwave.app.ui.components.StudioBadge

import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioEmerald
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedTracksBottomSheet(
    downloadedTracks: List<DownloadedTrack>,
    totalStorageFormatted: String,
    onDismiss: () -> Unit,
    onPlayTrack: (DownloadedTrack) -> Unit,
    onDeleteTrack: (String) -> Unit,
    onDeleteAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var trackToDelete by remember { mutableStateOf<DownloadedTrack?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    val filteredTracks = remember(downloadedTracks, searchQuery) {
        if (searchQuery.isBlank()) downloadedTracks
        else {
            val q = searchQuery.trim().lowercase()
            downloadedTracks.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
        }
    }

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
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StudioElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Свернуть",
                        tint = Zinc100,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Скачанные треки",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Zinc100
                    )
                    Text(
                        text = "${downloadedTracks.size} треков • $totalStorageFormatted",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioEmerald
                    )
                }

                if (downloadedTracks.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteAllConfirm = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StudioRed.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Удалить все",
                            tint = StudioRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(38.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                placeholder = { Text("Поиск по скачанным трекам...", fontSize = 12.sp, color = Zinc500) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = Zinc400,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Очистить",
                                tint = Zinc400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StudioSurface,
                    unfocusedContainerColor = StudioSurface,
                    focusedBorderColor = StudioAccent,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = Zinc100,
                    unfocusedTextColor = Zinc100,
                    cursorColor = StudioAccent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Storage Summary Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = StudioEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Память устройства",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc100
                        )
                        Text(
                            text = "Оффлайн-кэш доступен без подключения к сети",
                            fontSize = 10.sp,
                            color = Zinc400
                        )
                    }
                }

                StudioBadge(
                    text = totalStorageFormatted,
                    backgroundColor = StudioEmerald.copy(alpha = 0.15f),
                    textColor = StudioEmerald
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Downloaded Tracks
            if (filteredTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(StudioElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownloadDone,
                                contentDescription = null,
                                tint = Zinc500,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "Ничего не найдено" else "Нет скачанных треков",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Zinc300
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Попробуйте изменить запрос" else "Скачайте треки в плеере или через множественный выбор в медиатеке",
                            fontSize = 11.sp,
                            color = Zinc500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(filteredTracks, key = { _, t -> t.id }) { index, track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioSurface)
                                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                                .clickable { onPlayTrack(track) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc500,
                                    modifier = Modifier.width(22.dp)
                                )

                                val coverFile = remember(track.localCoverPath) {
                                    track.localCoverPath?.let { File(it) }?.takeIf { it.exists() && it.length() > 0 }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioElevated)
                                        .border(0.5.dp, StudioBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (coverFile != null) {
                                        AsyncImage(
                                            model = coverFile,
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Zinc500,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Zinc100,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = track.artist.ifBlank { "Unknown Artist" },
                                            fontSize = 10.sp,
                                            color = Zinc400,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "•",
                                            fontSize = 9.sp,
                                            color = Zinc500
                                        )
                                        Text(
                                            text = track.formattedSize,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = StudioEmerald
                                        )
                                    }
                                }

                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StudioBadge(
                                    text = track.format.uppercase().ifBlank { "AUDIO" },
                                    backgroundColor = StudioElevated,
                                    textColor = Zinc400
                                )

                                Text(
                                    text = track.formattedDuration,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Zinc400
                                )

                                // Play Button
                                IconButton(
                                    onClick = { onPlayTrack(track) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Играть",
                                        tint = StudioAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete from device button
                                IconButton(
                                    onClick = { trackToDelete = track },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Удалить с устройства",
                                        tint = StudioRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Single Track
    if (trackToDelete != null) {
        val tr = trackToDelete!!
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Удаление с устройства", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Удалить трек '${tr.title}' из памяти устройства? (С сервера трек удален не будет).", color = Zinc400, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTrack(tr.id)
                    trackToDelete = null
                }) {
                    Text("Удалить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }

    // Confirm Delete All Downloaded Tracks
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text("Удалить все скачанные треки", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Вы действительно хотите удалить ВСЕ скачанные треки (${downloadedTracks.size}) с устройства? Освободится $totalStorageFormatted.", color = Zinc400, fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAll()
                    showDeleteAllConfirm = false
                }) {
                    Text("Удалить все", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }
}

