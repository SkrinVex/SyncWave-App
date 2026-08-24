package com.SkrinVex.syncwave.app.ui.screens.library

import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.model.TrackStats

enum class ViewMode {
    LIST,
    GRID
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val totalTracks: Int = 0,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val stats: TrackStats = TrackStats(),
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylistId: String = "",
    val searchQuery: String = "",
    val sortBy: String = "created_at",
    val sortOrder: String = "desc",
    val viewMode: ViewMode = ViewMode.LIST,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val trackToDelete: Track? = null,
    val selectedTrackIds: Set<String> = emptySet(),
    val isBatchDeleteConfirmOpen: Boolean = false,
    val downloadedTrackIds: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val isSelectionMode: Boolean
        get() = selectedTrackIds.isNotEmpty()
}
