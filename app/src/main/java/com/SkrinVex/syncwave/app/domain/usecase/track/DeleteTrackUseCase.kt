package com.SkrinVex.syncwave.app.domain.usecase.track

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository

class DeleteTrackUseCase(private val trackRepository: TrackRepository) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        return trackRepository.deleteTrack(id)
    }

    suspend fun batch(ids: List<String>): Resource<Unit> {
        return trackRepository.batchDeleteTracks(ids)
    }
}
