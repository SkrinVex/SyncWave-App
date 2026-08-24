package com.SkrinVex.syncwave.app.domain.usecase.auth

import com.SkrinVex.syncwave.app.domain.model.AuthSession
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetSavedSessionUseCase(private val authRepository: AuthRepository) {
    val sessionFlow: Flow<AuthSession?> = authRepository.sessionFlow
    val serverUrlFlow: Flow<String> = authRepository.serverUrlFlow

    suspend operator fun invoke(): AuthSession? {
        return authRepository.getSavedSession()
    }
}
