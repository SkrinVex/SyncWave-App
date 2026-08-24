package com.SkrinVex.syncwave.app.ui.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.DownloadStatus
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.usecase.sync.CancelSyncUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.ClearSyncLogsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncLogsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncProgressUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.TriggerSyncUseCase
import com.SkrinVex.syncwave.app.download.DownloadManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SyncViewModel(
    private val getSyncProgressUseCase: GetSyncProgressUseCase,
    private val getSyncLogsUseCase: GetSyncLogsUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase,
    private val cancelSyncUseCase: CancelSyncUseCase,
    private val clearSyncLogsUseCase: ClearSyncLogsUseCase,
    val downloadManager: DownloadManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
        observeLocalDownloads()
    }

    private fun observeLocalDownloads() {
        viewModelScope.launch {
            downloadManager.isDownloading.collectLatest { downloading ->
                _uiState.update { it.copy(isLocalDownloading = downloading) }
            }
        }
        viewModelScope.launch {
            downloadManager.tasks.collectLatest { taskList ->
                val active = taskList.firstOrNull { it.status == DownloadStatus.DOWNLOADING }
                _uiState.update {
                    it.copy(
                        localDownloadTasks = taskList,
                        activeLocalTask = active
                    )
                }
            }
        }
        viewModelScope.launch {
            downloadManager.overallProgress.collectLatest { prog ->
                _uiState.update { it.copy(localOverallProgress = prog) }
            }
        }
        viewModelScope.launch {
            downloadManager.downloadedTracks.collectLatest { list ->
                val totalBytes = list.sumOf { it.sizeBytes }
                val mb = totalBytes.toDouble() / (1024 * 1024)
                val formatted = if (mb >= 1024) "%.2f ГБ".format(mb / 1024) else "%.1f МБ".format(mb)
                _uiState.update {
                    it.copy(
                        downloadedCount = list.size,
                        downloadedTotalStorageFormatted = formatted
                    )
                }
            }
        }
        viewModelScope.launch {
            downloadManager.isCancelled.collectLatest { cancelled ->
                _uiState.update { it.copy(isLastRunCancelled = cancelled) }
            }
        }
        viewModelScope.launch {
            downloadManager.cancelledSavedCount.collectLatest { count ->
                _uiState.update { it.copy(cancelledSavedCount = count) }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchProgress()
                fetchLogs()
                delay(2000L) // 2s polling like web
            }
        }
    }

    private suspend fun fetchProgress() {
        when (val result = getSyncProgressUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(progress = result.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }
    }

    private suspend fun fetchLogs() {
        when (val result = getSyncLogsUseCase(100)) {
            is Resource.Success -> {
                _uiState.update { it.copy(logs = result.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }
    }

    fun selectLogLevel(level: String) {
        _uiState.update { it.copy(selectedLogLevel = level) }
    }

    fun triggerSyncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggering = true) }
            when (val result = triggerSyncUseCase()) {
                is Resource.Success -> {
                    fetchProgress()
                    fetchLogs()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
            _uiState.update { it.copy(isTriggering = false) }
        }
    }

    fun cancelSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            when (val result = cancelSyncUseCase()) {
                is Resource.Success -> {
                    fetchProgress()
                    fetchLogs()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
            _uiState.update { it.copy(isCancelling = false) }
        }
    }

    fun cancelLocalDownloads() {
        downloadManager.cancelAll()
    }

    fun openConfirmClearLogs() {
        _uiState.update { it.copy(isConfirmClearLogsOpen = true) }
    }

    fun closeConfirmClearLogs() {
        _uiState.update { it.copy(isConfirmClearLogsOpen = false) }
    }

    fun clearLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingLogs = true, isConfirmClearLogsOpen = false) }
            when (val result = clearSyncLogsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(logs = emptyList()) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
            _uiState.update { it.copy(isClearingLogs = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        pollingJob = null
    }

    class Factory(
        private val getSyncProgressUseCase: GetSyncProgressUseCase,
        private val getSyncLogsUseCase: GetSyncLogsUseCase,
        private val triggerSyncUseCase: TriggerSyncUseCase,
        private val cancelSyncUseCase: CancelSyncUseCase,
        private val clearSyncLogsUseCase: ClearSyncLogsUseCase,
        private val downloadManager: DownloadManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SyncViewModel(
                getSyncProgressUseCase,
                getSyncLogsUseCase,
                triggerSyncUseCase,
                cancelSyncUseCase,
                clearSyncLogsUseCase,
                downloadManager
            ) as T
        }
    }
}
