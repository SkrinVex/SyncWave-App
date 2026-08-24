package com.SkrinVex.syncwave.app.ui.screens.playlists

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.ui.components.StudioBadge
import com.SkrinVex.syncwave.app.ui.components.StudioButton
import com.SkrinVex.syncwave.app.ui.components.StudioCard
import com.SkrinVex.syncwave.app.ui.components.StudioTextField
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

@Composable
fun PlaylistsScreen(
    viewModel: PlaylistsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }

    val context = LocalContext.current
    val uploadManager = SyncWaveApplication.instance.container.uploadManager
    var targetUploadPlaylistId by remember { mutableStateOf("") }

    val audioMimeTypes = arrayOf(
        "audio/*",
        "application/ogg",
        "audio/mpeg",
        "audio/mp4",
        "audio/flac",
        "audio/wav",
        "audio/aac",
        "audio/x-m4a",
        "audio/opus",
        "audio/x-ms-wma"
    )

    // Direct File Picker for Header / Playlist card
    val directFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            uploadManager.enqueueUploads(context, uris, targetUploadPlaylistId)
        }
    }

    // Modal Audio Picker for Manual Playlist Creation
    val modalFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.setSelectedAudioFiles(uris)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Плейлисты",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Zinc100
                )
                Text(
                    text = "Синхронизация и аудиотека",
                    fontSize = 12.sp,
                    color = Zinc400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Action Buttons (Upload & Add Playlist)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Upload Tracks Button (Emerald)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioEmerald)
                        .clickable {
                            targetUploadPlaylistId = ""
                            directFilePickerLauncher.launch(audioMimeTypes)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Загрузить",
                        tint = Zinc100,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Загрузить",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Add Playlist Button (Accent)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioAccent)
                        .clickable { viewModel.openAddModal() }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Zinc100,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Добавить",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc100,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playlists List
        if (uiState.isLoading && uiState.playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StudioAccent, strokeWidth = 2.dp)
            }
        } else if (uiState.playlists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = Zinc500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "Нет добавленных плейлистов",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc300
                    )
                    Text(
                        text = "Вы можете добавить плейлист YouTube Music или создать ручной плейлист со своими треками",
                        fontSize = 12.sp,
                        color = Zinc500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        StudioButton(
                            text = "Загрузить треки",
                            onClick = {
                                targetUploadPlaylistId = ""
                                directFilePickerLauncher.launch(audioMimeTypes)
                            },
                            backgroundColor = StudioEmerald,
                            textColor = Zinc100,
                            icon = Icons.Default.FileUpload,
                            modifier = Modifier.width(150.dp)
                        )

                        StudioButton(
                            text = "Создать плейлист",
                            onClick = { viewModel.openAddModal() },
                            backgroundColor = StudioAccent,
                            textColor = Zinc100,
                            icon = Icons.Default.Add,
                            modifier = Modifier.width(160.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.playlists, key = { it.id }) { playlist ->
                    PlaylistItemCard(
                        playlist = playlist,
                        isSyncing = uiState.syncingPlaylistId == playlist.id,
                        onSync = { viewModel.syncPlaylist(playlist.id) },
                        onDelete = { playlistToDelete = playlist },
                        onUploadToPlaylist = {
                            targetUploadPlaylistId = playlist.id
                            directFilePickerLauncher.launch(audioMimeTypes)
                        }
                    )
                }
            }
        }
    }

    // Add Playlist Modal Dialog (Dual Mode: YouTube Sync vs Manual Upload)
    if (uiState.isAddModalOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddModal() },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Добавить плейлист",
                    color = Zinc100,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Segmented Type Selector (YouTube Sync vs Manual)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioElevated)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // YouTube Sync Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) StudioAccent else StudioElevated)
                                .clickable { viewModel.setCreationType(PlaylistCreationType.YOUTUBE_SYNC) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) Zinc100 else Zinc400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "YouTube Синхр.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) Zinc100 else Zinc400
                                )
                            }
                        }

                        // Manual Upload Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.creationType == PlaylistCreationType.MANUAL_UPLOAD) StudioEmerald else StudioElevated)
                                .clickable { viewModel.setCreationType(PlaylistCreationType.MANUAL_UPLOAD) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (uiState.creationType == PlaylistCreationType.MANUAL_UPLOAD) Zinc100 else Zinc400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Свои треки",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.creationType == PlaylistCreationType.MANUAL_UPLOAD) Zinc100 else Zinc400
                                )
                            }
                        }
                    }

                    if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) {
                        // MODE 1: YouTube Music Sync

                        // Quick Preset Button: Liked Music (LM)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioElevated)
                                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = StudioRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Пресет: Понравившиеся (LM)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Zinc100
                                    )
                                    Text(
                                        text = "Liked Music из YouTube Music",
                                        fontSize = 10.sp,
                                        color = Zinc400
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioAccent.copy(alpha = 0.2f))
                                    .clickable { viewModel.setPresetLikedMusic() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Применить",
                                    fontSize = 11.sp,
                                    color = StudioAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // URL or ID
                        StudioTextField(
                            value = uiState.newUrlOrId,
                            onValueChange = { viewModel.onNewUrlOrIdChange(it) },
                            label = "URL или ID плейлиста *",
                            placeholder = "LM или https://music.youtube.com/playlist?list=..."
                        )

                        // Title (Optional)
                        StudioTextField(
                            value = uiState.newTitle,
                            onValueChange = { viewModel.onNewTitleChange(it) },
                            label = "Название (опционально)",
                            placeholder = "Автоматически подтянется из YouTube"
                        )

                        // Auto Sync Switch + Interval Selection (Web Parity)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioElevated)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Автосинхронизация",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Zinc100
                                    )
                                    Text(
                                        text = "Периодическая проверка новых треков",
                                        fontSize = 10.sp,
                                        color = Zinc400
                                    )
                                }

                                Switch(
                                    checked = uiState.newAutoSync,
                                    onCheckedChange = { viewModel.onNewAutoSyncChange(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Zinc100,
                                        checkedTrackColor = StudioAccent,
                                        uncheckedThumbColor = Zinc500,
                                        uncheckedTrackColor = StudioSurface
                                    )
                                )
                            }

                            // Interval Preset Chips
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (uiState.newAutoSync) 1f else 0.35f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Zinc400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Интервал проверки:",
                                        fontSize = 11.sp,
                                        color = Zinc300,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                val intervals = listOf(
                                    15 to "15м",
                                    30 to "30м",
                                    60 to "1ч",
                                    360 to "6ч",
                                    720 to "12ч",
                                    1440 to "24ч"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    intervals.forEach { (mins, label) ->
                                        val isSelected = uiState.newIntervalMinutes == mins
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected && uiState.newAutoSync) StudioAccent else StudioSurface)
                                                .clickable(enabled = uiState.newAutoSync) {
                                                    viewModel.onNewIntervalMinutesChange(mins)
                                                }
                                                .padding(vertical = 5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected && uiState.newAutoSync) Zinc100 else Zinc400
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // MODE 2: Manual Playlist Upload

                        StudioTextField(
                            value = uiState.newTitle,
                            onValueChange = { viewModel.onNewTitleChange(it) },
                            label = "Название плейлиста *",
                            placeholder = "Например: Моя коллекция, Альбом 2026..."
                        )

                        // File Selector Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioElevated)
                                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = StudioEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Аудиофайлы для плейлиста",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Zinc100
                                    )
                                }

                                if (uiState.selectedAudioCount > 0) {
                                    StudioBadge(
                                        text = "${uiState.selectedAudioCount} выбрано",
                                        backgroundColor = StudioEmerald.copy(alpha = 0.2f),
                                        textColor = StudioEmerald
                                    )
                                }
                            }

                            Text(
                                text = if (uiState.selectedAudioCount > 0) {
                                    "Выбрано ${uiState.selectedAudioCount} аудиофайлов. Они начнут загружаться сразу после создания."
                                } else {
                                    "Вы можете выбрать файлы прямо сейчас или загрузить их позже в созданный плейлист."
                                },
                                fontSize = 11.sp,
                                color = Zinc400,
                                lineHeight = 15.sp
                            )

                            // Pick files button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, StudioEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { modalFilePickerLauncher.launch(audioMimeTypes) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = StudioEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.selectedAudioCount > 0) "Изменить выбор файлов" else "Выбрать аудиофайлы",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StudioEmerald
                                )
                            }
                        }
                    }

                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 11.sp,
                            color = StudioRed
                        )
                    }
                }
            },
            confirmButton = {
                val buttonText = if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) {
                    "Добавить"
                } else {
                    if (uiState.selectedAudioCount > 0) "Создать и загрузить" else "Создать"
                }

                StudioButton(
                    text = buttonText,
                    onClick = {
                        viewModel.createPlaylist { playlistId, uris ->
                            uploadManager.enqueueUploads(context, uris, playlistId)
                        }
                    },
                    backgroundColor = if (uiState.creationType == PlaylistCreationType.YOUTUBE_SYNC) StudioAccent else StudioEmerald,
                    isLoading = uiState.isCreating,
                    modifier = Modifier.wrapContentWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddModal() }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }

    // Delete Playlist Confirmation Dialog
    if (playlistToDelete != null) {
        val pl = playlistToDelete!!
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = StudioSurface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Удаление плейлиста",
                    color = Zinc100,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Вы действительно хотите удалить плейлист «${pl.title}»? Сами треки останутся в вашей медиатеке.",
                    color = Zinc400,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = pl.id
                    playlistToDelete = null
                    viewModel.deletePlaylist(id)
                }) {
                    Text("Удалить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }
}

@Composable
fun PlaylistItemCard(
    playlist: Playlist,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onDelete: () -> Unit,
    onUploadToPlaylist: () -> Unit
) {
    val isManualPlaylist = playlist.youtubeId.equals("MANUAL", ignoreCase = true) || playlist.youtubeId.startsWith("manual:")

    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = StudioSurface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = playlist.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Zinc100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StudioBadge(
                        text = if (isManualPlaylist) "РУЧНОЙ" else playlist.youtubeId,
                        backgroundColor = if (isManualPlaylist) StudioEmerald.copy(alpha = 0.15f) else StudioElevated,
                        textColor = if (isManualPlaylist) StudioEmerald else StudioAccent
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${playlist.trackCount} треков",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc400
                    )
                    if (playlist.autoSync && !isManualPlaylist) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StudioEmerald)
                            )
                            val intervalText = if (playlist.syncIntervalMinutes >= 60) {
                                val h = playlist.syncIntervalMinutes / 60
                                val m = playlist.syncIntervalMinutes % 60
                                if (m == 0) "$h ч" else "$h ч $m мин"
                            } else {
                                "${playlist.syncIntervalMinutes} мин"
                            }
                            Text(
                                text = "Авто ($intervalText)",
                                fontSize = 10.sp,
                                color = StudioEmerald
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Upload to Playlist Button
                IconButton(onClick = onUploadToPlaylist, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Загрузить треки в этот плейлист",
                        tint = StudioEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sync Button (Only for YouTube Playlists)
                if (!isManualPlaylist) {
                    IconButton(onClick = onSync, enabled = !isSyncing, modifier = Modifier.size(32.dp)) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = StudioAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Синхронизировать",
                                tint = StudioAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Delete Button
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Zinc500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
