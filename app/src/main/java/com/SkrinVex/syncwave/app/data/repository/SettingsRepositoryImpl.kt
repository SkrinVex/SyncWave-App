package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val apiService: SyncWaveApiService
) : SettingsRepository {

    override suspend fun getSettings(): Resource<SystemSettings> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSettings()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                Resource.Error("Ошибка загрузки настроек: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }
}
