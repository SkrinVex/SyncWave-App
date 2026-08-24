package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    override suspend fun uploadCookies(content: ByteArray): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val requestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("cookies", "cookies.txt", requestBody)
            val response = apiService.uploadCookiesMultipart(part)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Не удалось сохранить cookies на сервере (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка загрузки cookies: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun uploadCookiesText(cookiesText: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        uploadCookies(cookiesText.toByteArray(Charsets.UTF_8))
    }

    override suspend fun deleteCookies(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteCookies()
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Не удалось удалить cookies (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка удаления cookies: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun testProxy(proxyUrl: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.testProxy(mapOf("proxy_url" to proxyUrl))
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Прокси не отвечает или заблокирован (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка проверки прокси: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }
}
