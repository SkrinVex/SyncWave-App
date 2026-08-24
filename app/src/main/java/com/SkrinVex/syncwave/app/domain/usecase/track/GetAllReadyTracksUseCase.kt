package com.SkrinVex.syncwave.app.domain.usecase.track

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository

class GetAllReadyTracksUseCase(
    private val trackRepository: TrackRepository
) {
    suspend operator fun invoke(playlistId: String? = null): Resource<List<Track>> {
        return trackRepository.getAllReadyTracks(playlistId)
    }
}
