package com.SkrinVex.syncwave.app.ui.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncLogsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.GetSyncProgressUseCase
import com.SkrinVex.syncwave.app.domain.usecase.sync.TriggerSyncUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SyncViewModel(
    private val getSyncProgressUseCase: GetSyncProgressUseCase,
    private val getSyncLogsUseCase: GetSyncLogsUseCase,
    private val triggerSyncUseCase: TriggerSyncUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                pollTelemetry()
                delay(2000L) // Poll every 2 seconds
            }
        }
    }

    private suspend fun pollTelemetry() {
        // Poll progress
        when (val pResult = getSyncProgressUseCase()) {
            is Resource.Success -> {
                _uiState.update { it.copy(progress = pResult.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }

        // Poll logs
        when (val lResult = getSyncLogsUseCase(50)) {
            is Resource.Success -> {
                _uiState.update { it.copy(logs = lResult.data) }
            }
            is Resource.Error -> {}
            Resource.Loading -> {}
        }
    }

    fun triggerSyncAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTriggering = true) }
            triggerSyncUseCase()
            _uiState.update { it.copy(isTriggering = false) }
            pollTelemetry()
        }
    }

    fun cancelSync() {
        viewModelScope.launch {
            triggerSyncUseCase.cancel()
            pollTelemetry()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            getSyncLogsUseCase.clear()
            _uiState.update { it.copy(logs = emptyList()) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    class Factory(
        private val getSyncProgressUseCase: GetSyncProgressUseCase,
        private val getSyncLogsUseCase: GetSyncLogsUseCase,
        private val triggerSyncUseCase: TriggerSyncUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SyncViewModel(
                getSyncProgressUseCase,
                getSyncLogsUseCase,
                triggerSyncUseCase
            ) as T
        }
    }
}
