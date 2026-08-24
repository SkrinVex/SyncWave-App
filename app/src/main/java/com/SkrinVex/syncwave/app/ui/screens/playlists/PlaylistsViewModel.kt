package com.SkrinVex.syncwave.app.ui.screens.playlists

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Playlist
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
                creationType = PlaylistCreationType.YOUTUBE_SYNC,
                newTitle = "",
                newUrlOrId = "",
                newAutoSync = true,
                newIntervalMinutes = 60,
                selectedAudioUris = emptyList(),
                selectedAudioCount = 0,
                errorMessage = null
            )
        }
    }

    fun closeAddModal() {
        _uiState.update { it.copy(isAddModalOpen = false) }
    }

    fun setCreationType(type: PlaylistCreationType) {
        _uiState.update { it.copy(creationType = type, errorMessage = null) }
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

    fun onNewIntervalMinutesChange(minutes: Int) {
        _uiState.update { it.copy(newIntervalMinutes = minutes) }
    }

    fun setSelectedAudioFiles(uris: List<Uri>) {
        _uiState.update {
            it.copy(
                selectedAudioUris = uris,
                selectedAudioCount = uris.size
            )
        }
    }

    fun removeSelectedAudioFile(index: Int) {
        _uiState.update { state ->
            val updated = state.selectedAudioUris.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            state.copy(selectedAudioUris = updated, selectedAudioCount = updated.size)
        }
    }

    fun createPlaylist(onSuccessUpload: ((playlistId: String, uris: List<Uri>) -> Unit)? = null) {
        val state = _uiState.value

        if (state.creationType == PlaylistCreationType.YOUTUBE_SYNC) {
            if (state.newUrlOrId.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Укажите ссылку или ID плейлиста YouTube") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isCreating = true, errorMessage = null) }
                val result = createPlaylistUseCase(
                    title = state.newTitle,
                    urlOrId = state.newUrlOrId,
                    autoSync = state.newAutoSync,
                    syncIntervalMinutes = if (state.newAutoSync) state.newIntervalMinutes else 0
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
        } else {
            // MANUAL_UPLOAD
            if (state.newTitle.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Введите название плейлиста") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isCreating = true, errorMessage = null) }
                val result = createPlaylistUseCase(
                    title = state.newTitle,
                    urlOrId = "MANUAL",
                    autoSync = false,
                    syncIntervalMinutes = 0
                )

                when (result) {
                    is Resource.Success -> {
                        val playlist = result.data
                        val urisToUpload = state.selectedAudioUris
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                isAddModalOpen = false,
                                selectedAudioUris = emptyList(),
                                selectedAudioCount = 0
                            )
                        }
                        fetchPlaylists()
                        if (urisToUpload.isNotEmpty()) {
                            onSuccessUpload?.invoke(playlist.id, urisToUpload)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isCreating = false, errorMessage = result.message) }
                    }
                    Resource.Loading -> {}
                }
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
