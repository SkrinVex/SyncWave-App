package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.model.TrackStats

data class TrackListResult(
    val tracks: List<Track>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

interface TrackRepository {
    suspend fun getTracks(
        query: String? = null,
        playlistId: String? = null,
        status: String? = null,
        sortBy: String = "created_at",
        order: String = "desc",
        page: Int = 1,
        pageSize: Int = 50
    ): Resource<TrackListResult>

    suspend fun getAllReadyTracks(playlistId: String? = null): Resource<List<Track>>
    suspend fun getStats(): Resource<TrackStats>
    suspend fun getTrack(id: String): Resource<Track>
    suspend fun deleteTrack(id: String): Resource<Unit>
    suspend fun batchDeleteTracks(ids: List<String>): Resource<Unit>
    fun getStreamUrl(trackId: String, token: String): String
    fun getCoverUrl(trackId: String, token: String): String
}
