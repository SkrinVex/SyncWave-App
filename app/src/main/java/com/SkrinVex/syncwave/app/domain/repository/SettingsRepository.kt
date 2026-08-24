package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SystemSettings

interface SettingsRepository {
    suspend fun getSettings(): Resource<SystemSettings>
    suspend fun uploadCookies(content: ByteArray): Resource<Boolean>
    suspend fun uploadCookiesText(cookiesText: String): Resource<Boolean>
    suspend fun deleteCookies(): Resource<Boolean>
    suspend fun testProxy(proxyUrl: String): Resource<Boolean>
}
