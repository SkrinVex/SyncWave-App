package com.SkrinVex.syncwave.app.ui.screens.sync

import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.model.SyncProgress

data class SyncUiState(
    val progress: SyncProgress = SyncProgress(),
    val logs: List<SyncLog> = emptyList(),
    val selectedLogLevel: String = "ALL",
    val isTriggering: Boolean = false,
    val isCancelling: Boolean = false,
    val isClearingLogs: Boolean = false,
    val isConfirmClearLogsOpen: Boolean = false,
    val errorMessage: String? = null
)
