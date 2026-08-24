package com.SkrinVex.syncwave.app.ui.screens.sync

import com.SkrinVex.syncwave.app.domain.model.DownloadTask
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
    val isLocalDownloading: Boolean = false,
    val isLastRunCancelled: Boolean = false,
    val cancelledSavedCount: Int = 0,
    val localDownloadTasks: List<DownloadTask> = emptyList(),
    val activeLocalTask: DownloadTask? = null,
    val localOverallProgress: Int = 0,
    val downloadedCount: Int = 0,
    val downloadedTotalStorageFormatted: String = "0 МБ",
    val errorMessage: String? = null
)
