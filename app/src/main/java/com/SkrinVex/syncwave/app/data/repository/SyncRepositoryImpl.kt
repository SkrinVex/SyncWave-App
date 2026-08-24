package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.model.SyncProgress
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepositoryImpl(
    private val apiService: SyncWaveApiService
) : SyncRepository {

    override suspend fun getProgress(): Resource<SyncProgress> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSyncProgress()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Ошибка получения статуса синхронизации: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun getLogs(limit: Int): Resource<List<SyncLog>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSyncLogs(limit)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.map { it.toDomain() })
            } else {
                Resource.Error("Ошибка загрузки логов")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun triggerSyncAll(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.triggerSyncAll()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось запустить синхронизацию")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun cancelSync(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.cancelSync()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось отменить синхронизацию")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }

    override suspend fun clearLogs(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.clearSyncLogs()
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Не удалось очистить логи")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage}", e)
        }
    }
}
