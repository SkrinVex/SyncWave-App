package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SystemSettings

interface SettingsRepository {
    suspend fun getSettings(): Resource<SystemSettings>
}
