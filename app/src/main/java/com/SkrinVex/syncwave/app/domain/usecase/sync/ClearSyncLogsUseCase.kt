package com.SkrinVex.syncwave.app.domain.usecase.sync

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository

class ClearSyncLogsUseCase(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return syncRepository.clearLogs()
    }
}
