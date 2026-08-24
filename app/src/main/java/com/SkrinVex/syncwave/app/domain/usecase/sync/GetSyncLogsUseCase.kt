package com.SkrinVex.syncwave.app.domain.usecase.sync

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository

class GetSyncLogsUseCase(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(limit: Int = 100): Resource<List<SyncLog>> {
        return syncRepository.getLogs(limit)
    }

    suspend fun clear(): Resource<Unit> {
        return syncRepository.clearLogs()
    }
}
