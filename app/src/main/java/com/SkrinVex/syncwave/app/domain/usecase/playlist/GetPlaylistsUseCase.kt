package com.SkrinVex.syncwave.app.domain.usecase.playlist

import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository

class GetPlaylistsUseCase(private val playlistRepository: PlaylistRepository) {
    suspend operator fun invoke(): Resource<List<Playlist>> {
        return playlistRepository.getPlaylists()
    }
}
