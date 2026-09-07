package com.SkrinVex.syncwave.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.DownloadStatus
import com.SkrinVex.syncwave.app.ui.components.FullPlayerBottomSheet
import com.SkrinVex.syncwave.app.ui.components.MiniPlayerBar
import com.SkrinVex.syncwave.app.ui.components.OfflinePlaceholderScreen
import com.SkrinVex.syncwave.app.ui.navigation.Screen
import com.SkrinVex.syncwave.app.ui.screens.library.LibraryScreen
import com.SkrinVex.syncwave.app.ui.screens.library.LibraryViewModel
import com.SkrinVex.syncwave.app.ui.screens.playlists.PlaylistsScreen
import com.SkrinVex.syncwave.app.ui.screens.playlists.PlaylistsViewModel
import com.SkrinVex.syncwave.app.ui.screens.settings.SettingsScreen
import com.SkrinVex.syncwave.app.ui.screens.settings.SettingsViewModel
import com.SkrinVex.syncwave.app.ui.screens.sync.SyncScreen
import com.SkrinVex.syncwave.app.ui.screens.sync.SyncViewModel
import com.SkrinVex.syncwave.app.ui.theme.StudioAccent
import com.SkrinVex.syncwave.app.ui.theme.StudioBg
import com.SkrinVex.syncwave.app.ui.theme.StudioBorder
import com.SkrinVex.syncwave.app.ui.theme.StudioSurface
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun MainScreen(
    onNavigateToAuth: () -> Unit
) {
    val navController = rememberNavController()
    val container = SyncWaveApplication.instance.container
    val playerManager = container.audioPlayerManager
    val downloadManager = container.downloadManager

    // Only the *identity* of the playing track is observed here. Position, buffering and
    // download progress tick several times a second; collecting them at this level used to
    // recompose the whole Scaffold, the navigation bar and the NavHost on every tick.
    // They are collected inside the player hosts below instead.
    val currentTrack by playerManager.currentTrackFlow.collectAsStateWithLifecycle(initialValue = null)

    var isFullPlayerOpen by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Library.route

    val token by container.sessionDataStore.tokenFlow.collectAsStateWithLifecycle(initialValue = "")
    val isOnline by container.networkConnectivityObserver.isOnline.collectAsStateWithLifecycle(initialValue = true)

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(StudioBg)) {
                // Upload Progress Drawer (floating widget)
                com.SkrinVex.syncwave.app.ui.components.UploadProgressDrawer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                // Mini Player Bar
                currentTrack?.let { track ->
                    MiniPlayerHost(
                        playerManager = playerManager,
                        coverModel = remember(track.id, token) {
                            container.trackRepository.getCoverModel(track.id, token ?: "")
                        },
                        onExpand = { isFullPlayerOpen = true }
                    )
                }

                // Studio Bottom Navigation Bar
                NavigationBar(
                    containerColor = StudioSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val items = listOf(
                        Screen.Library,
                        Screen.Playlists,
                        Screen.Sync,
                        Screen.Settings
                    )

                    items.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Library.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon ?: Icons.Default.LibraryMusic,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    color = if (selected) StudioAccent else Zinc500
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StudioAccent,
                                unselectedIconColor = Zinc500,
                                selectedTextColor = StudioAccent,
                                unselectedTextColor = Zinc500,
                                indicatorColor = StudioBorder.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBg)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Library.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Library.route) {
                    val viewModel: LibraryViewModel = viewModel(
                        factory = LibraryViewModel.Factory(
                            container.getTracksUseCase,
                            container.getAllReadyTracksUseCase,
                            container.getLibraryStatsUseCase,
                            container.getPlaylistsUseCase,
                            container.deleteTrackUseCase,
                            container.batchDeleteTracksUseCase,
                            downloadManager,
                            playerManager,
                            container.networkConnectivityObserver
                        )
                    )
                    LibraryScreen(viewModel = viewModel)
                }

                composable(Screen.Playlists.route) {
                    if (!isOnline) {
                        OfflinePlaceholderScreen(
                            tabTitle = "Плейлисты",
                            onGoToLibrary = {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(Screen.Library.route) { inclusive = false }
                                }
                            }
                        )
                    } else {
                        val viewModel: PlaylistsViewModel = viewModel(
                            factory = PlaylistsViewModel.Factory(
                                container.getPlaylistsUseCase,
                                container.createPlaylistUseCase,
                                container.deletePlaylistUseCase,
                                container.syncPlaylistUseCase
                            )
                        )
                        PlaylistsScreen(viewModel = viewModel)
                    }
                }

                composable(Screen.Sync.route) {
                    if (!isOnline) {
                        OfflinePlaceholderScreen(
                            tabTitle = "Синхронизация",
                            onGoToLibrary = {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(Screen.Library.route) { inclusive = false }
                                }
                            }
                        )
                    } else {
                        val viewModel: SyncViewModel = viewModel(
                            factory = SyncViewModel.Factory(
                                container.getSyncProgressUseCase,
                                container.getSyncLogsUseCase,
                                container.triggerSyncUseCase,
                                container.cancelSyncUseCase,
                                container.clearSyncLogsUseCase,
                                downloadManager
                            )
                        )
                        SyncScreen(viewModel = viewModel)
                    }
                }

                composable(Screen.Settings.route) {
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.Factory(
                            container.getCurrentUserUseCase,
                            container.getSettingsUseCase,
                            container.getLibraryStatsUseCase,
                            container.getServerUrlUseCase,
                            container.saveServerUrlUseCase,
                            container.logoutUseCase,
                            container.settingsRepository,
                            container.sessionDataStore,
                            downloadManager,
                            playerManager
                        )
                    )
                    SettingsScreen(
                        viewModel = viewModel,
                        isOffline = !isOnline,
                        onNavigateToAuth = onNavigateToAuth
                    )
                }
            }
        }
    }

    // Expandable Full Player Bottom Sheet
    val fullPlayerTrack = currentTrack
    if (isFullPlayerOpen && fullPlayerTrack != null) {
        FullPlayerHost(
            playerManager = playerManager,
            downloadManager = downloadManager,
            track = fullPlayerTrack,
            token = token ?: "",
            onDismiss = { isFullPlayerOpen = false }
        )
    }
}

/**
 * Owns the frequently changing player state so that only the mini player recomposes
 * while playback position advances.
 */
@Composable
private fun MiniPlayerHost(
    playerManager: com.SkrinVex.syncwave.app.player.AudioPlayerManager,
    coverModel: Any,
    onExpand: () -> Unit
) {
    val playerState by playerManager.playerState.collectAsStateWithLifecycle()
    if (playerState.currentTrack == null) return

    MiniPlayerBar(
        playerState = playerState,
        coverModel = coverModel,
        onExpand = onExpand,
        onPlayPause = { playerManager.togglePlayPause() },
        onNext = { playerManager.playNext() },
        onDismiss = { playerManager.stopPlayback() }
    )
}

/**
 * Same idea for the expanded player: position updates and download progress stay
 * contained in this composable instead of invalidating the whole screen.
 */
@Composable
private fun FullPlayerHost(
    playerManager: com.SkrinVex.syncwave.app.player.AudioPlayerManager,
    downloadManager: com.SkrinVex.syncwave.app.download.DownloadManager,
    track: com.SkrinVex.syncwave.app.domain.model.Track,
    token: String,
    onDismiss: () -> Unit
) {
    val container = SyncWaveApplication.instance.container
    val playerState by playerManager.playerState.collectAsStateWithLifecycle()
    val downloadedTrackIds by downloadManager.downloadedTrackIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val downloadTasks by downloadManager.tasks.collectAsStateWithLifecycle(initialValue = emptyList())

    if (playerState.currentTrack == null) return

    val coverModel = remember(track.id, token) {
        container.trackRepository.getCoverModel(track.id, token)
    }
    // Cover lookups are memory-only, but keeping one lambda identity avoids
    // re-running them for every queue row on each recomposition.
    val getTrackCoverModel = remember(token) {
        { trackId: String -> container.trackRepository.getCoverModel(trackId, token) }
    }

    val isDownloaded = downloadedTrackIds.contains(track.id)
    val currentTask = downloadTasks.firstOrNull { it.id == track.id }
    val isDownloading = currentTask?.status == DownloadStatus.DOWNLOADING || currentTask?.status == DownloadStatus.PENDING
    val downloadProgress = currentTask?.progress ?: 0

    FullPlayerBottomSheet(
        playerState = playerState,
        coverModel = coverModel,
        getTrackCoverModel = getTrackCoverModel,
        onDismiss = onDismiss,
        onPlayPause = { playerManager.togglePlayPause() },
        onNext = { playerManager.playNext() },
        onPrevious = { playerManager.playPrevious() },
        onSeek = { targetMs -> playerManager.seekTo(targetMs) },
        onToggleShuffle = { playerManager.toggleShuffle() },
        onCycleRepeat = { playerManager.cycleRepeatMode() },
        onRewind10 = { playerManager.rewind10Seconds() },
        onForward10 = { playerManager.forward10Seconds() },
        onSetPlaybackSpeed = { speed -> playerManager.setPlaybackSpeed(speed) },
        onSelectQueueTrack = { index -> playerManager.skipToQueueItem(index) },
        onRemoveFromQueue = { index -> playerManager.removeFromQueue(index) },
        onReshuffleQueue = { playerManager.reshuffleQueue() },
        onClearQueue = { playerManager.clearQueue() },
        isDownloaded = isDownloaded,
        isDownloading = isDownloading,
        downloadProgress = downloadProgress,
        onDownloadTrack = { downloadManager.enqueueDownload(track) },
        onDeleteDownloadedTrack = { downloadManager.deleteDownloadedTrack(track.id) }
    )
}

