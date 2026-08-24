package com.SkrinVex.syncwave.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.usecase.playlist.GetPlaylistsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.DeleteTrackUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetAllReadyTracksUseCase
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
    private val getAllReadyTracksUseCase: GetAllReadyTracksUseCase,
    private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val deleteTrackUseCase: DeleteTrackUseCase,
    val playerManager: AudioPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var isFetchingPage = false

    companion object {
        const val PAGE_SIZE = 300
    }

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
            fetchTracks(page = 1, isInitial = true)

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

    fun fetchTracks(page: Int = 1, isInitial: Boolean = false) {
        if (isFetchingPage) return
        isFetchingPage = true

        viewModelScope.launch {
            val state = _uiState.value
            val result = getTracksUseCase(
                query = state.searchQuery,
                playlistId = state.selectedPlaylistId,
                sortBy = state.sortBy,
                order = state.sortOrder,
                page = page,
                pageSize = PAGE_SIZE
            )

            when (result) {
                is Resource.Success -> {
                    val newTracks = if (page == 1) result.data.tracks else state.tracks + result.data.tracks
                    val hasMore = newTracks.size < result.data.total

                    _uiState.update {
                        it.copy(
                            tracks = newTracks,
                            totalTracks = result.data.total,
                            currentPage = page,
                            hasMore = hasMore,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message,
                            isLoadingMore = false
                        )
                    }
                }
                Resource.Loading -> {}
            }
            isFetchingPage = false
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.hasMore && !isFetchingPage && !state.isLoading && !state.isLoadingMore) {
            _uiState.update { it.copy(isLoadingMore = true) }
            fetchTracks(page = state.currentPage + 1)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            fetchTracks(page = 1, isInitial = false)
        }
    }

    fun selectPlaylist(playlistId: String) {
        _uiState.update { it.copy(selectedPlaylistId = playlistId) }
        fetchTracks(page = 1, isInitial = false)
    }

    fun setSortBy(sortBy: String) {
        _uiState.update { it.copy(sortBy = sortBy) }
        fetchTracks(page = 1, isInitial = false)
    }

    fun toggleSortOrder() {
        val nextOrder = if (_uiState.value.sortOrder == "asc") "desc" else "asc"
        _uiState.update { it.copy(sortOrder = nextOrder) }
        fetchTracks(page = 1, isInitial = false)
    }

    fun toggleViewMode() {
        val nextMode = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
        _uiState.update { it.copy(viewMode = nextMode) }
    }

    fun playTrack(track: Track, index: Int) {
        viewModelScope.launch {
            val playlistId = _uiState.value.selectedPlaylistId.ifBlank { null }
            val fullReadyResult = getAllReadyTracksUseCase(playlistId)
            val queueToUse = when (fullReadyResult) {
                is Resource.Success -> if (fullReadyResult.data.isNotEmpty()) fullReadyResult.data else _uiState.value.tracks
                else -> _uiState.value.tracks
            }
            playerManager.playTrack(track, customQueue = queueToUse)
        }
    }

    fun playAll(shuffle: Boolean) {
        viewModelScope.launch {
            val playlistId = _uiState.value.selectedPlaylistId.ifBlank { null }
            val fullReadyResult = getAllReadyTracksUseCase(playlistId)
            val queueToUse = when (fullReadyResult) {
                is Resource.Success -> if (fullReadyResult.data.isNotEmpty()) fullReadyResult.data else _uiState.value.tracks
                else -> _uiState.value.tracks
            }
            if (queueToUse.isNotEmpty()) {
                playerManager.playTrackList(queueToUse, startIndex = 0, shuffle = shuffle)
            }
        }
    }

    fun addToQueue(track: Track) {
        playerManager.addToQueue(track)
    }

    fun playNext(track: Track) {
        playerManager.playNextInQueue(track)
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
        private val getAllReadyTracksUseCase: GetAllReadyTracksUseCase,
        private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
        private val getPlaylistsUseCase: GetPlaylistsUseCase,
        private val deleteTrackUseCase: DeleteTrackUseCase,
        private val playerManager: AudioPlayerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(
                getTracksUseCase,
                getAllReadyTracksUseCase,
                getLibraryStatsUseCase,
                getPlaylistsUseCase,
                deleteTrackUseCase,
                playerManager
            ) as T
        }
    }
}
