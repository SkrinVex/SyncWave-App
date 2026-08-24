package com.SkrinVex.syncwave.app.domain.usecase.auth

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository
import com.SkrinVex.syncwave.app.domain.repository.AuthStatus

class CheckAuthStatusUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(serverUrl: String? = null): Resource<AuthStatus> {
        return authRepository.checkStatus(serverUrl)
    }
}
