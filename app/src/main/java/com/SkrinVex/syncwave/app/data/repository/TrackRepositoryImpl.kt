package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.BatchDeleteRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.model.TrackStats
import com.SkrinVex.syncwave.app.domain.repository.TrackListResult
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepositoryImpl(
    private val apiService: SyncWaveApiService,
    private val sessionDataStore: SessionDataStore
) : TrackRepository {

    override suspend fun getTracks(
        query: String?,
        playlistId: String?,
        status: String?,
        sortBy: String,
        order: String,
        page: Int,
        pageSize: Int
    ): Resource<TrackListResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTracks(
                query = query?.takeIf { it.isNotBlank() },
                playlistId = playlistId?.takeIf { it.isNotBlank() },
                status = status?.takeIf { it.isNotBlank() },
                sortBy = sortBy,
                order = order,
                page = page,
                pageSize = pageSize
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Ошибка получения треков: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка загрузки треков: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun getAllReadyTracks(playlistId: String?): Resource<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllReadyTracks(playlistId?.takeIf { it.isNotBlank() })
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.map { it.toDomain() })
            } else {
                Resource.Error("Ошибка получения готовых треков: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun getStats(): Resource<TrackStats> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrackStats()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Ошибка загрузки статистики: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun getTrack(id: String): Resource<Track> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrack(id)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Трек не найден")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun deleteTrack(id: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTrack(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось удалить трек")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка удаления: ${e.localizedMessage}", e)
        }
    }

    override suspend fun batchDeleteTracks(ids: List<String>): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.batchDeleteTracks(BatchDeleteRequestDto(ids))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось удалить выбранные треки")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка массового удаления: ${e.localizedMessage}", e)
        }
    }

    override fun getStreamUrl(trackId: String, token: String): String {
        val serverUrl = sessionDataStore.getServerUrlCached().trimEnd('/')
        return "$serverUrl/api/v1/tracks/$trackId/stream?token=$token"
    }

    override fun getCoverUrl(trackId: String, token: String): String {
        val serverUrl = sessionDataStore.getServerUrlCached().trimEnd('/')
        return "$serverUrl/api/v1/tracks/$trackId/cover?token=$token"
    }
}
