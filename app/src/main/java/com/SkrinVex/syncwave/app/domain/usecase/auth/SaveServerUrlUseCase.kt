package com.SkrinVex.syncwave.app.domain.usecase.auth

import com.SkrinVex.syncwave.app.domain.repository.AuthRepository

class SaveServerUrlUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.dropLast(1)
        }
        authRepository.setServerUrl(cleanUrl)
    }
}
