package com.SkrinVex.syncwave.app.data.repository

import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.data.remote.dto.AuthRequestDto
import com.SkrinVex.syncwave.app.data.remote.dto.ErrorResponseDto
import com.SkrinVex.syncwave.app.data.remote.dto.toDomain
import com.SkrinVex.syncwave.app.domain.model.AuthSession
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.User
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository
import com.SkrinVex.syncwave.app.domain.repository.AuthStatus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class AuthRepositoryImpl(
    private val apiService: SyncWaveApiService,
    private val sessionDataStore: SessionDataStore,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson = Gson()
) : AuthRepository {

    override val sessionFlow: Flow<AuthSession?> = sessionDataStore.sessionFlow
    override val serverUrlFlow: Flow<String> = sessionDataStore.serverUrlFlow

    override suspend fun getServerUrl(): String = sessionDataStore.getServerUrl()

    override suspend fun setServerUrl(url: String) = sessionDataStore.saveServerUrl(url)

    override suspend fun checkStatus(serverUrl: String?): Resource<AuthStatus> = withContext(Dispatchers.IO) {
        try {
            val currentUrl = serverUrl?.takeIf { it.isNotBlank() } ?: getServerUrl()
            val cleanUrl = currentUrl.trim().trimEnd('/')

            val targetUrl = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                "https://$cleanUrl"
            } else {
                cleanUrl
            }

            val request = Request.Builder()
                .url("$targetUrl/api/v1/auth/status")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                val dto = gson.fromJson(bodyStr, com.SkrinVex.syncwave.app.data.remote.dto.AuthStatusDto::class.java)
                Resource.Success(dto.toDomain())
            } else {
                Resource.Error("Сервер вернул ошибку: ${response.code}")
            }
        } catch (e: Exception) {
            Resource.Error("Не удалось подключиться к серверу: ${e.localizedMessage ?: "Проверьте адрес и сеть"}", e)
        }
    }

    override suspend fun login(username: String, password: String): Resource<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(AuthRequestDto(username, password))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val user = dto.user.toDomain()
                sessionDataStore.saveSession(dto.token, user)
                val serverUrl = getServerUrl()
                Resource.Success(AuthSession(dto.token, user, serverUrl))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(errorMsg ?: "Неверное имя пользователя или пароль")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка авторизации: ${e.localizedMessage ?: "Проверьте подключение к серверу"}", e)
        }
    }

    override suspend fun setupAdmin(username: String, password: String): Resource<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.setupAdmin(AuthRequestDto(username, password))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val user = dto.user.toDomain()
                sessionDataStore.saveSession(dto.token, user)
                val serverUrl = getServerUrl()
                Resource.Success(AuthSession(dto.token, user, serverUrl))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Resource.Error(errorMsg ?: "Не удалось создать учетную запись администратора")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка создания администратора: ${e.localizedMessage ?: "Проверьте подключение к серверу"}", e)
        }
    }

    override suspend fun getMe(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomain()
                val session = sessionDataStore.getSavedSession()
                if (session != null) {
                    sessionDataStore.saveSession(session.token, user)
                }
                Resource.Success(user)
            } else if (response.code() == 401) {
                logout()
                Resource.Error("Сессия истекла. Пожалуйста, войдите снова.")
            } else {
                Resource.Error("Не удалось получить профиль: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Ошибка загрузки профиля: ${e.localizedMessage ?: "Сетевая ошибка"}", e)
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        sessionDataStore.clearSession()
    }

    override suspend fun getSavedSession(): AuthSession? = withContext(Dispatchers.IO) {
        sessionDataStore.getSavedSession()
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val errDto = gson.fromJson(errorBody, ErrorResponseDto::class.java)
            errDto.error ?: errorBody
        } catch (e: Exception) {
            errorBody
        }
    }
}
