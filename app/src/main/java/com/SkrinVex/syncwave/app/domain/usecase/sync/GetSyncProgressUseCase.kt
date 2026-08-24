package com.SkrinVex.syncwave.app.domain.usecase.sync

import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.model.SyncProgress
import com.SkrinVex.syncwave.app.domain.repository.SyncRepository

class GetSyncProgressUseCase(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(): Resource<SyncProgress> {
        return syncRepository.getProgress()
    }
}
