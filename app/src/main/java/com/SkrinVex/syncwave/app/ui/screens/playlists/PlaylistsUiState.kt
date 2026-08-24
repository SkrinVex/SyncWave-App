package com.SkrinVex.syncwave.app.ui.screens.playlists

import android.net.Uri
import com.SkrinVex.syncwave.app.domain.model.Playlist

enum class PlaylistCreationType {
    YOUTUBE_SYNC,
    MANUAL_UPLOAD
}

data class PlaylistsUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val isAddModalOpen: Boolean = false,
    val creationType: PlaylistCreationType = PlaylistCreationType.YOUTUBE_SYNC,
    val newTitle: String = "",
    val newUrlOrId: String = "",
    val newAutoSync: Boolean = true,
    val newIntervalMinutes: Int = 60,
    val selectedAudioUris: List<Uri> = emptyList(),
    val selectedAudioCount: Int = 0,
    val isCreating: Boolean = false,
    val syncingPlaylistId: String? = null,
    val errorMessage: String? = null
)
