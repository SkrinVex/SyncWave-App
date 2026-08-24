package com.SkrinVex.syncwave.app.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.usecase.playlist.CreatePlaylistUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.DeletePlaylistUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.GetPlaylistsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.playlist.SyncPlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val syncPlaylistUseCase: SyncPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistsUiState())
    val uiState: StateFlow<PlaylistsUiState> = _uiState.asStateFlow()

    init {
        fetchPlaylists()
    }

    fun fetchPlaylists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPlaylistsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, playlists = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
        }
    }

    fun openAddModal() {
        _uiState.update {
            it.copy(
                isAddModalOpen = true,
                newTitle = "",
                newUrlOrId = "",
                newAutoSync = true,
                newIntervalMinutes = 60,
                errorMessage = null
            )
        }
    }

    fun closeAddModal() {
        _uiState.update { it.copy(isAddModalOpen = false) }
    }

    fun setPresetLikedMusic() {
        _uiState.update {
            it.copy(
                newTitle = "Понравившиеся",
                newUrlOrId = "LM"
            )
        }
    }

    fun onNewTitleChange(title: String) {
        _uiState.update { it.copy(newTitle = title) }
    }

    fun onNewUrlOrIdChange(urlOrId: String) {
        _uiState.update { it.copy(newUrlOrId = urlOrId) }
    }

    fun onNewAutoSyncChange(autoSync: Boolean) {
        _uiState.update { it.copy(newAutoSync = autoSync) }
    }

    fun createPlaylist() {
        val state = _uiState.value
        if (state.newTitle.isBlank() || state.newUrlOrId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Заполните все обязательные поля") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            val result = createPlaylistUseCase(
                title = state.newTitle,
                urlOrId = state.newUrlOrId,
                autoSync = state.newAutoSync,
                syncIntervalMinutes = state.newIntervalMinutes
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isCreating = false, isAddModalOpen = false) }
                    fetchPlaylists()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isCreating = false, errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
        }
    }

    fun syncPlaylist(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingPlaylistId = id) }
            syncPlaylistUseCase(id)
            _uiState.update { it.copy(syncingPlaylistId = null) }
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            when (deletePlaylistUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(playlists = state.playlists.filter { it.id != id })
                    }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }
        }
    }

    class Factory(
        private val getPlaylistsUseCase: GetPlaylistsUseCase,
        private val createPlaylistUseCase: CreatePlaylistUseCase,
        private val deletePlaylistUseCase: DeletePlaylistUseCase,
        private val syncPlaylistUseCase: SyncPlaylistUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistsViewModel(
                getPlaylistsUseCase,
                createPlaylistUseCase,
                deletePlaylistUseCase,
                syncPlaylistUseCase
            ) as T
        }
    }
}
