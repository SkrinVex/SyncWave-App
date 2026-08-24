package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.AuthSession
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.User
import kotlinx.coroutines.flow.Flow

data class AuthStatus(
    val needsSetup: Boolean,
    val allowRegistration: Boolean
)

interface AuthRepository {
    val sessionFlow: Flow<AuthSession?>
    val serverUrlFlow: Flow<String>

    suspend fun getServerUrl(): String
    suspend fun setServerUrl(url: String)
    suspend fun checkStatus(serverUrl: String? = null): Resource<AuthStatus>
    suspend fun login(username: String, password: String): Resource<AuthSession>
    suspend fun setupAdmin(username: String, password: String): Resource<AuthSession>
    suspend fun getMe(): Resource<User>
    suspend fun logout()
    suspend fun getSavedSession(): AuthSession?
}
