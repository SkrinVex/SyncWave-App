package com.SkrinVex.syncwave.app.domain.usecase.auth

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.User
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Resource<User> {
        return authRepository.getMe()
    }
}
