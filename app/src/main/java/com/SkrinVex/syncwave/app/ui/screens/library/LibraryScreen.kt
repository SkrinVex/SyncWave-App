package com.SkrinVex.syncwave.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.ui.components.StudioSoundwaveLogo
import com.SkrinVex.syncwave.app.ui.components.TrackCardItem
import com.SkrinVex.syncwave.app.ui.components.TrackRowItem
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioElevated
import com.SkrinVex.syncwave.app.ui.theme.StudioRed
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc300
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500
import com.SkrinVex.syncwave.app.ui.theme.Zinc950
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTrackId by viewModel.playerManager.currentTrackIdFlow.collectAsStateWithLifecycle(initialValue = null)
    val isPlaying by viewModel.playerManager.isPlayingFlow.collectAsStateWithLifecycle(initialValue = false)
    val token by SyncWaveApplication.instance.container.sessionDataStore.tokenFlow.collectAsStateWithLifecycle(initialValue = "")

    var showSortMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Smooth Infinite scroll detection for List View using snapshotFlow
    LaunchedEffect(listState, uiState.hasMore, uiState.isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 6
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && uiState.hasMore && !uiState.isLoadingMore) {
                    viewModel.loadNextPage()
                }
            }
    }

    // Smooth Infinite scroll detection for Grid View using snapshotFlow
    LaunchedEffect(gridState, uiState.hasMore, uiState.isLoadingMore) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 6
        }
            .distinctUntilChanged()
            .collect { shouldLoad ->
                if (shouldLoad && uiState.hasMore && !uiState.isLoadingMore) {
                    viewModel.loadNextPage()
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Header (Title and Status)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Медиатека",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Zinc100
                )
                Text(
                    text = "Загружено треков: ${uiState.tracks.size} из ${uiState.totalTracks}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Zinc400,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            placeholder = { Text("Поиск по трекам, авторам, альбомам...", fontSize = 13.sp, color = Zinc500) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск",
                    tint = Zinc400,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить",
                            tint = Zinc400,
                            modifier = Modifier.size(18.dp)
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

        // Actions Row (Play All, Shuffle All, Sort, View Mode)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play & Shuffle All Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play All
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioAccent)
                        .clickable { viewModel.playAll(false) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Zinc100,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Все", color = Zinc100, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Shuffle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioElevated)
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.playAll(true) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = Zinc400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("Перемешать", color = Zinc300, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Sort & View Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sort Dropdown Trigger
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioElevated)
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable { showSortMenu = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (uiState.sortBy) {
                                "title" -> "Название"
                                "artist" -> "Артист"
                                "duration" -> "Время"
                                else -> "Новые"
                            },
                            fontSize = 11.sp,
                            color = Zinc300
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(StudioElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("По дате добавления", color = Zinc100, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setSortBy("created_at")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По названию", color = Zinc100, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setSortBy("title")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По исполнителю", color = Zinc100, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setSortBy("artist")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("По длительности", color = Zinc100, fontSize = 12.sp) },
                            onClick = {
                                viewModel.setSortBy("duration")
                                showSortMenu = false
                            }
                        )
                    }
                }

                // Sort Order Toggle Button (Asc / Desc)
                IconButton(
                    onClick = { viewModel.toggleSortOrder() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.sortOrder == "asc") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Порядок сортировки",
                        tint = Zinc400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // View Mode Switcher (List / Grid)
                IconButton(
                    onClick = { viewModel.toggleViewMode() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "Вид",
                        tint = StudioAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playlist Filter Chips (Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAllSelected = uiState.selectedPlaylistId.isBlank()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isAllSelected) Zinc100 else StudioElevated)
                    .border(1.dp, if (isAllSelected) Zinc100 else StudioBorder, RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectPlaylist("") }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Все треки (${uiState.stats.totalTracks})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAllSelected) Zinc950 else Zinc400
                )
            }

            // Playlist Chips
            uiState.playlists.forEach { pl ->
                val isSelected = uiState.selectedPlaylistId == pl.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Zinc100 else StudioElevated)
                        .border(1.dp, if (isSelected) Zinc100 else StudioBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectPlaylist(pl.id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pl.title} (${pl.trackCount})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Zinc950 else Zinc400
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area (Loading with Logo, Empty, List or Grid with Optimized Scroll)
        if (uiState.isLoading && uiState.tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StudioSoundwaveLogo(
                        size = 52.dp,
                        isAnimated = true
                    )
                    Text(
                        text = "Загрузка медиатеки...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Zinc400
                    )
                }
            }
        } else if (uiState.tracks.isEmpty()) {
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
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Zinc500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "В медиатеке пусто",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Zinc300
                    )
                    Text(
                        text = "Добавьте плейлисты на вкладке 'Плейлисты' или запустите синхронизацию",
                        fontSize = 12.sp,
                        color = Zinc500,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (uiState.viewMode == ViewMode.GRID) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = uiState.tracks,
                    key = { _, track -> track.id },
                    contentType = { _, _ -> "track_card" }
                ) { index, track ->
                    val isCurrent = currentTrackId == track.id
                    val coverUrl = SyncWaveApplication.instance.container.trackRepository.getCoverUrl(track.id, token ?: "")

                    TrackCardItem(
                        track = track,
                        coverUrl = coverUrl,
                        isPlaying = isPlaying,
                        isCurrentTrack = isCurrent,
                        onClick = { viewModel.playTrack(track, index) }
                    )
                }

                if (uiState.isLoadingMore) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StudioAccent,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = uiState.tracks,
                    key = { _, track -> track.id },
                    contentType = { _, _ -> "track_row" }
                ) { index, track ->
                    val isCurrent = currentTrackId == track.id
                    val coverUrl = SyncWaveApplication.instance.container.trackRepository.getCoverUrl(track.id, token ?: "")

                    TrackRowItem(
                        track = track,
                        coverUrl = coverUrl,
                        isPlaying = isPlaying,
                        isCurrentTrack = isCurrent,
                        index = index + 1,
                        onClick = { viewModel.playTrack(track, index) },
                        onDelete = { viewModel.confirmDeleteTrack(track) }
                    )
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = StudioAccent,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Track Confirmation Dialog
    if (uiState.trackToDelete != null) {
        val track = uiState.trackToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = StudioSurface,
            title = {
                Text("Удаление трека", color = Zinc100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Вы действительно хотите удалить '${track.title}' с сервера? Файл аудиозаписи будет удален безвозвратно.",
                    color = Zinc400,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.executeDeleteTrack() }) {
                    Text("Удалить", color = StudioRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Отмена", color = Zinc400)
                }
            }
        )
    }
}
