package com.SkrinVex.syncwave.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetCurrentUserUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetSavedSessionUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.LogoutUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SaveServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.settings.GetSettingsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SettingsEvent {
    data object NavigateToAuth : SettingsEvent()
}

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val getSavedSessionUseCase: GetSavedSessionUseCase,
    private val getServerUrlUseCase: GetServerUrlUseCase,
    private val saveServerUrlUseCase: SaveServerUrlUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val session = getSavedSessionUseCase()
            val serverUrl = getServerUrlUseCase()
            _uiState.update {
                it.copy(
                    user = session?.user,
                    serverUrl = serverUrl,
                    newServerUrl = serverUrl
                )
            }

            // Fetch live user & settings
            when (val uResult = getCurrentUserUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(user = uResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }

            when (val sResult = getSettingsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(settings = sResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }
        }
    }

    fun openEditServerUrlModal() {
        _uiState.update {
            it.copy(
                isEditServerUrlModalOpen = true,
                newServerUrl = it.serverUrl
            )
        }
    }

    fun closeEditServerUrlModal() {
        _uiState.update { it.copy(isEditServerUrlModalOpen = false) }
    }

    fun onNewServerUrlChange(url: String) {
        _uiState.update { it.copy(newServerUrl = url) }
    }

    fun saveServerUrl() {
        viewModelScope.launch {
            val url = _uiState.value.newServerUrl
            saveServerUrlUseCase(url)
            val updatedUrl = getServerUrlUseCase()
            _uiState.update {
                it.copy(
                    serverUrl = updatedUrl,
                    isEditServerUrlModalOpen = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _events.emit(SettingsEvent.NavigateToAuth)
        }
    }

    class Factory(
        private val getCurrentUserUseCase: GetCurrentUserUseCase,
        private val getSettingsUseCase: GetSettingsUseCase,
        private val getSavedSessionUseCase: GetSavedSessionUseCase,
        private val getServerUrlUseCase: GetServerUrlUseCase,
        private val saveServerUrlUseCase: SaveServerUrlUseCase,
        private val logoutUseCase: LogoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                getCurrentUserUseCase,
                getSettingsUseCase,
                getSavedSessionUseCase,
                getServerUrlUseCase,
                saveServerUrlUseCase,
                logoutUseCase
            ) as T
        }
    }
}
