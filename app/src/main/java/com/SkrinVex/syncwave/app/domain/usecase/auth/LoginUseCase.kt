package com.SkrinVex.syncwave.app.domain.usecase.auth

import com.SkrinVex.syncwave.app.domain.model.AuthSession
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Resource<AuthSession> {
        if (username.isBlank()) return Resource.Error("Введите имя пользователя")
        if (password.isBlank()) return Resource.Error("Введите пароль")
        if (username.length < 3) return Resource.Error("Имя пользователя должно содержать не менее 3 символов")
        if (password.length < 6) return Resource.Error("Пароль должен содержать не менее 6 символов")

        return authRepository.login(username.trim(), password)
    }
}
