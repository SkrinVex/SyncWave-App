package com.SkrinVex.syncwave.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
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
import com.SkrinVex.syncwave.app.ui.components.FullPlayerBottomSheet
import com.SkrinVex.syncwave.app.ui.components.MiniPlayerBar
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
import com.SkrinVex.syncwave.app.ui.theme.Zinc100
import com.SkrinVex.syncwave.app.ui.theme.Zinc400
import com.SkrinVex.syncwave.app.ui.theme.Zinc500

@Composable
fun MainScreen(
    onNavigateToAuth: () -> Unit
) {
    val navController = rememberNavController()
    val container = SyncWaveApplication.instance.container
    val playerManager = container.audioPlayerManager
    val playerState by playerManager.playerState.collectAsStateWithLifecycle()

    var isFullPlayerOpen by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Library.route

    val token by container.sessionDataStore.tokenFlow.collectAsStateWithLifecycle(initialValue = "")

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(StudioBg)) {
                // Mini Player Bar
                if (playerState.currentTrack != null) {
                    val coverUrl = container.trackRepository.getCoverUrl(playerState.currentTrack!!.id, token ?: "")
                    MiniPlayerBar(
                        playerState = playerState,
                        coverUrl = coverUrl,
                        onExpand = { isFullPlayerOpen = true },
                        onPlayPause = { playerManager.togglePlayPause() },
                        onNext = { playerManager.playNext() }
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
                            container.getLibraryStatsUseCase,
                            container.getPlaylistsUseCase,
                            container.deleteTrackUseCase,
                            playerManager
                        )
                    )
                    LibraryScreen(viewModel = viewModel)
                }

                composable(Screen.Playlists.route) {
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

                composable(Screen.Sync.route) {
                    val viewModel: SyncViewModel = viewModel(
                        factory = SyncViewModel.Factory(
                            container.getSyncProgressUseCase,
                            container.getSyncLogsUseCase,
                            container.triggerSyncUseCase
                        )
                    )
                    SyncScreen(viewModel = viewModel)
                }

                composable(Screen.Settings.route) {
                    val viewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.Factory(
                            container.getCurrentUserUseCase,
                            container.getSettingsUseCase,
                            container.getSavedSessionUseCase,
                            container.getServerUrlUseCase,
                            container.saveServerUrlUseCase,
                            container.logoutUseCase
                        )
                    )
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAuth = onNavigateToAuth
                    )
                }
            }
        }
    }

    // Expandable Full Player Bottom Sheet
    if (isFullPlayerOpen && playerState.currentTrack != null) {
        val coverUrl = container.trackRepository.getCoverUrl(playerState.currentTrack!!.id, token ?: "")
        FullPlayerBottomSheet(
            playerState = playerState,
            coverUrl = coverUrl,
            onDismiss = { isFullPlayerOpen = false },
            onPlayPause = { playerManager.togglePlayPause() },
            onNext = { playerManager.playNext() },
            onPrevious = { playerManager.playPrevious() },
            onSeek = { targetMs -> playerManager.seekTo(targetMs) },
            onToggleShuffle = { playerManager.toggleShuffle() },
            onCycleRepeat = { playerManager.cycleRepeatMode() }
        )
    }
}
