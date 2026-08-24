package com.SkrinVex.syncwave.app.ui.screens.playlists

import com.SkrinVex.syncwave.app.domain.model.Playlist

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val isAddModalOpen: Boolean = false,
    val newTitle: String = "",
    val newUrlOrId: String = "",
    val newAutoSync: Boolean = true,
    val newIntervalMinutes: Int = 60,
    val isCreating: Boolean = false,
    val syncingPlaylistId: String? = null,
    val errorMessage: String? = null
)
