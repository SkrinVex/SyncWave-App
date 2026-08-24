package com.SkrinVex.syncwave.app.domain.usecase.playlist

import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository

class CreatePlaylistUseCase(private val playlistRepository: PlaylistRepository) {
    suspend operator fun invoke(title: String, urlOrId: String, autoSync: Boolean = true, syncIntervalMinutes: Int = 60): Resource<Playlist> {
        if (title.isBlank()) return Resource.Error("Введите название плейлиста")
        if (urlOrId.isBlank()) return Resource.Error("Введите ID или ссылку плейлиста")
        return playlistRepository.createPlaylist(title.trim(), urlOrId.trim(), autoSync, syncIntervalMinutes)
    }
}
