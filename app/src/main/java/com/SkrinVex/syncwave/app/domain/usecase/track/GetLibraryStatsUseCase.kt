package com.SkrinVex.syncwave.app.domain.usecase.track

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.TrackStats
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository

class GetLibraryStatsUseCase(private val trackRepository: TrackRepository) {
    suspend operator fun invoke(): Resource<TrackStats> {
        return trackRepository.getStats()
    }
}
