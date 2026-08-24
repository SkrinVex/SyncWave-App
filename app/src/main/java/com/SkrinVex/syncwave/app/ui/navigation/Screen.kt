package com.SkrinVex.syncwave.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    data object Auth : Screen("auth")
    data object Main : Screen("main")

    // Tabs inside MainScreen
    data object Library : Screen("library", "Медиатека", Icons.Default.LibraryMusic)
    data object Playlists : Screen("playlists", "Плейлисты", Icons.Default.QueueMusic)
    data object Sync : Screen("sync", "Синхронизация", Icons.Default.GraphicEq)
    data object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}
