package com.SkrinVex.syncwave.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.usecase.playlist.GetPlaylistsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.DeleteTrackUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetLibraryStatsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetTracksUseCase
import com.SkrinVex.syncwave.app.player.AudioPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val getTracksUseCase: GetTracksUseCase,
    private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val deleteTrackUseCase: DeleteTrackUseCase,
    val playerManager: AudioPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            fetchStats()
            fetchPlaylists()
            fetchTracks()

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    private suspend fun fetchStats() {
        when (val result = getLibraryStatsUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(stats = result.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }
    }

    private suspend fun fetchPlaylists() {
        when (val result = getPlaylistsUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(playlists = result.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }
    }

    fun fetchTracks() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = getTracksUseCase(
                query = state.searchQuery,
                playlistId = state.selectedPlaylistId,
                sortBy = state.sortBy,
                order = state.sortOrder,
                page = 1,
                pageSize = 200
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            tracks = result.data.tracks,
                            totalTracks = result.data.total,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350L) // Debounce
            fetchTracks()
        }
    }

    fun selectPlaylist(playlistId: String) {
        _uiState.update { it.copy(selectedPlaylistId = playlistId) }
        fetchTracks()
    }

    fun setSortBy(sortBy: String) {
        _uiState.update { it.copy(sortBy = sortBy) }
        fetchTracks()
    }

    fun toggleSortOrder() {
        val nextOrder = if (_uiState.value.sortOrder == "asc") "desc" else "asc"
        _uiState.update { it.copy(sortOrder = nextOrder) }
        fetchTracks()
    }

    fun toggleViewMode() {
        val nextMode = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
        _uiState.update { it.copy(viewMode = nextMode) }
    }

    fun playTrack(track: Track, index: Int) {
        val tracks = _uiState.value.tracks
        playerManager.playTrackList(tracks, startIndex = index, shuffle = false)
    }

    fun playAll(shuffle: Boolean) {
        val tracks = _uiState.value.tracks
        if (tracks.isNotEmpty()) {
            playerManager.playTrackList(tracks, startIndex = 0, shuffle = shuffle)
        }
    }

    fun confirmDeleteTrack(track: Track) {
        _uiState.update { it.copy(trackToDelete = track) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(trackToDelete = null) }
    }

    fun executeDeleteTrack() {
        val track = _uiState.value.trackToDelete ?: return
        viewModelScope.launch {
            when (deleteTrackUseCase(track.id)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            tracks = state.tracks.filter { it.id != track.id },
                            totalTracks = (state.totalTracks - 1).coerceAtLeast(0),
                            trackToDelete = null
                        )
                    }
                    fetchStats()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(trackToDelete = null) }
                }
                Resource.Loading -> {}
            }
        }
    }

    class Factory(
        private val getTracksUseCase: GetTracksUseCase,
        private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
        private val getPlaylistsUseCase: GetPlaylistsUseCase,
        private val deleteTrackUseCase: DeleteTrackUseCase,
        private val playerManager: AudioPlayerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(
                getTracksUseCase,
                getLibraryStatsUseCase,
                getPlaylistsUseCase,
                deleteTrackUseCase,
                playerManager
            ) as T
        }
    }
}
