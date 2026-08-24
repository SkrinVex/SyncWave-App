package com.SkrinVex.syncwave.app.domain.usecase.playlist

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository

class DeletePlaylistUseCase(private val playlistRepository: PlaylistRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        return playlistRepository.deletePlaylist(id)
    }
}
