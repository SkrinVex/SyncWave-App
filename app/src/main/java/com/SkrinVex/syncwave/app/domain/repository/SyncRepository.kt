package com.SkrinVex.syncwave.app.domain.repository

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.model.SyncProgress

interface SyncRepository {
    suspend fun triggerSyncAll(): Resource<Unit>
    suspend fun cancelSync(): Resource<Unit>
    suspend fun getProgress(): Resource<SyncProgress>
    suspend fun getLogs(limit: Int = 100): Resource<List<SyncLog>>
    suspend fun clearLogs(): Resource<Unit>
}
