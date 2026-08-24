package com.SkrinVex.syncwave.app.domain.usecase.sync

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository

class TriggerSyncUseCase(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return syncRepository.triggerSyncAll()
    }

    suspend fun cancel(): Resource<Unit> {
        return syncRepository.cancelSync()
    }
}
