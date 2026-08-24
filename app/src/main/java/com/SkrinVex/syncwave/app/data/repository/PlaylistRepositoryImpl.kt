package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.CreatePlaylistRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.UpdatePlaylistRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val apiService: SyncWaveApiService
) : PlaylistRepository {

    override suspend fun getPlaylists(): Resource<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPlaylists()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.map { it.toDomain() })
            } else {
                Resource.Error("Ошибка загрузки плейлистов: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun createPlaylist(
        title: String,
        urlOrId: String,
        autoSync: Boolean,
        syncIntervalMinutes: Int
    ): Resource<Playlist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPlaylist(
                CreatePlaylistRequestDto(
                    title = title,
                    urlOrId = urlOrId,
                    autoSync = autoSync,
                    syncIntervalMinutes = syncIntervalMinutes
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Не удалось создать плейлист")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка создания: ${e.localizedMessage}", e)
        }
    }

    override suspend fun updatePlaylist(
        id: String,
        title: String,
        autoSync: Boolean,
        syncIntervalMinutes: Int
    ): Resource<Playlist> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePlaylist(
                id = id,
                body = UpdatePlaylistRequestDto(
                    title = title,
                    autoSync = autoSync,
                    syncIntervalMinutes = syncIntervalMinutes
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Не удалось обновить плейлист")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка обновления: ${e.localizedMessage}", e)
        }
    }

    override suspend fun deletePlaylist(id: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePlaylist(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось удалить плейлист")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка удаления: ${e.localizedMessage}", e)
        }
    }

    override suspend fun syncPlaylist(id: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.syncPlaylist(id)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось запустить синхронизацию плейлиста")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка запуска синхронизации: ${e.localizedMessage}", e)
        }
    }
}
