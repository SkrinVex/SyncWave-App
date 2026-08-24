package com.SkrinVex.syncwave.app.domain.usecase.settings

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.repository.SettingsRepository

class GetSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): Resource<SystemSettings> {
        return settingsRepository.getSettings()
    }
}
