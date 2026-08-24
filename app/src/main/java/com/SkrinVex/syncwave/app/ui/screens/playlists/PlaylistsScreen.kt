package com.SkrinVex.syncwave.app.ui.screens.playlists

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueueMusic
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            Column {
                Text(
                    text = "Плейлисты",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Zinc100
                )
                Text(
                    text = "Автосинхронизация с YouTube Music",
                    fontSize = 12.sp,
                    color = Zinc400,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Add Playlist Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioAccent)
                    .clickable { viewModel.openAddModal() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Zinc100,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Добавить",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Zinc100
                )
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            imageVector = Icons.Default.QueueMusic,
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
                        text = "Добавьте плейлист или 'Понравившиеся (LM)' для автосинхронизации",
                        fontSize = 12.sp,
                        color = Zinc500,
                        textAlign = TextAlign.Center
                    )
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
                        onDelete = { playlistToDelete = playlist }
                    )
                }
            }
        }
    }

    // Add Playlist Modal Dialog
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
                    // Quick Preset Button: Liked Music (LM)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setPresetLikedMusic() }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "Синхронизация Liked Music из YouTube Music",
                                fontSize = 10.sp,
                                color = Zinc400
                            )
                        }
                    }

                    // Title
                    StudioTextField(
                        value = uiState.newTitle,
                        onValueChange = { viewModel.onNewTitleChange(it) },
                        label = "Название плейлиста",
                        placeholder = "Понравившиеся или Мой Плейлист"
                    )

                    // URL or ID
                    StudioTextField(
                        value = uiState.newUrlOrId,
                        onValueChange = { viewModel.onNewUrlOrIdChange(it) },
                        label = "ID или ссылка на плейлист",
                        placeholder = "LM или PLxxxx..."
                    )

                    // Auto Sync Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Автосинхронизация",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Zinc100
                            )
                            Text(
                                text = "Периодическая проверка новых треков",
                                fontSize = 10.sp,
                                color = Zinc500
                            )
                        }

                        Switch(
                            checked = uiState.newAutoSync,
                            onCheckedChange = { viewModel.onNewAutoSyncChange(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Zinc100,
                                checkedTrackColor = StudioAccent,
                                uncheckedThumbColor = Zinc500,
                                uncheckedTrackColor = StudioElevated
                            )
                        )
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
                StudioButton(
                    text = "Добавить",
                    onClick = { viewModel.createPlaylist() },
                    isLoading = uiState.isCreating,
                    modifier = Modifier.width(110.dp)
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
    onDelete: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = playlist.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Zinc100
                    )
                    StudioBadge(
                        text = playlist.youtubeId,
                        backgroundColor = StudioElevated,
                        textColor = StudioAccent
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
                    if (playlist.autoSync) {
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
                // Sync Button
                IconButton(onClick = onSync, enabled = !isSyncing) {
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Delete Button
                IconButton(onClick = onDelete) {
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
