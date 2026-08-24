package com.SkrinVex.syncwave.app.ui.screens.sync

import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.model.SyncProgress

data class SyncUiState(
    val progress: SyncProgress = SyncProgress(),
    val logs: List<SyncLog> = emptyList(),
    val isTriggering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
