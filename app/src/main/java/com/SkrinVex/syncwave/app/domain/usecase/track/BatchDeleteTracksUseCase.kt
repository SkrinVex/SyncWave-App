package com.SkrinVex.syncwave.app.domain.usecase.track

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository

class BatchDeleteTracksUseCase(
    private val trackRepository: TrackRepository
) {
    suspend operator fun invoke(ids: List<String>): Resource<Unit> {
        return trackRepository.batchDeleteTracks(ids)
    }
}

