package com.SkrinVex.syncwave.app.domain.usecase.track

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.TrackListResult
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository

class GetTracksUseCase(private val trackRepository: TrackRepository) {
    suspend operator fun invoke(
        query: String? = null,
        playlistId: String? = null,
        status: String? = null,
        sortBy: String = "created_at",
        order: String = "desc",
        page: Int = 1,
        pageSize: Int = 50
    ): Resource<TrackListResult> {
        return trackRepository.getTracks(
            query = query,
            playlistId = playlistId,
            status = status,
            sortBy = sortBy,
            order = order,
            page = page,
            pageSize = pageSize
        )
    }
}
