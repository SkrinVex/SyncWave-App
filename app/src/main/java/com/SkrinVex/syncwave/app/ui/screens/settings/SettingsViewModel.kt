package com.SkrinVex.syncwave.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.repository.SettingsRepository
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetCurrentUserUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.LogoutUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SaveServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.settings.GetSettingsUseCase
import com.SkrinVex.syncwave.app.domain.usecase.track.GetLibraryStatsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

sealed interface SettingsEvent {
    object NavigateToAuth : SettingsEvent
}

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
    private val getServerUrlUseCase: GetServerUrlUseCase,
    private val saveServerUrlUseCase: SaveServerUrlUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val settingsRepository: SettingsRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        loadData()
        observeAudioFocus()
    }

    private fun observeAudioFocus() {
        viewModelScope.launch {
            sessionDataStore.audioFocusEnabledFlow.collectLatest { enabled ->
                _uiState.update { it.copy(isAudioFocusEnabled = enabled) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val url = getServerUrlUseCase()
            _uiState.update { it.copy(serverUrl = url, newServerUrl = url) }

            when (val userResult = getCurrentUserUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(user = userResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }

            when (val settingsResult = getSettingsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(settings = settingsResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }

            when (val statsResult = getLibraryStatsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(stats = statsResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refreshSettings() {
        viewModelScope.launch {
            when (val settingsResult = getSettingsUseCase()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(settings = settingsResult.data) }
                }
                is Resource.Error -> {}
                Resource.Loading -> {}
            }
        }
    }

    fun openGoogleAuthModal() {
        _uiState.update { it.copy(isGoogleAuthModalOpen = true) }
    }

    fun closeGoogleAuthModal() {
        _uiState.update { it.copy(isGoogleAuthModalOpen = false) }
    }

    fun uploadCookiesFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCookies = true, cookiesOperationMessage = null) }
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    when (val res = settingsRepository.uploadCookies(bytes)) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isUploadingCookies = false,
                                    cookiesOperationMessage = "Файл cookies.txt успешно загружен и применен!"
                                )
                            }
                            refreshSettings()
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isUploadingCookies = false,
                                    cookiesOperationMessage = "Ошибка: ${res.message}"
                                )
                            }
                        }
                        is Resource.Loading -> {}
                    }
                } else {
                    _uiState.update {
                        it.copy(isUploadingCookies = false, cookiesOperationMessage = "Файл пуст или недоступен")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploadingCookies = false, cookiesOperationMessage = "Ошибка чтения файла: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteCookies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCookies = true, cookiesOperationMessage = null) }
            when (val res = settingsRepository.deleteCookies()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingCookies = false,
                            cookiesOperationMessage = "Cookies успешно удалены с сервера"
                        )
                    }
                    refreshSettings()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUploadingCookies = false,
                            cookiesOperationMessage = "Ошибка: ${res.message}"
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleAudioFocus(enabled: Boolean) {
        viewModelScope.launch {
            sessionDataStore.setAudioFocusEnabled(enabled)
            _uiState.update { it.copy(isAudioFocusEnabled = enabled) }
        }
    }

    fun openEditServerUrlModal() {
        _uiState.update { it.copy(isEditServerUrlModalOpen = true, newServerUrl = it.serverUrl) }
    }

    fun closeEditServerUrlModal() {
        _uiState.update { it.copy(isEditServerUrlModalOpen = false) }
    }

    fun onNewServerUrlChange(url: String) {
        _uiState.update { it.copy(newServerUrl = url) }
    }

    fun saveServerUrl() {
        val newUrl = _uiState.value.newServerUrl.trim()
        if (newUrl.isNotBlank()) {
            viewModelScope.launch {
                saveServerUrlUseCase(newUrl)
                _uiState.update {
                    it.copy(
                        serverUrl = newUrl,
                        isEditServerUrlModalOpen = false,
                        connectionTestResult = null
                    )
                }
                testConnection()
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            val url = _uiState.value.serverUrl.trimEnd('/') + "/api/v1/health"
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(url).build()
                val startTime = System.currentTimeMillis()
                val response = client.newCall(request).execute()
                val duration = System.currentTimeMillis() - startTime
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            connectionTestResult = "Подключение успешно • ${duration} мс"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            connectionTestResult = "Ошибка ответа сервера: HTTP ${response.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = "Ошибка соединения: ${e.localizedMessage ?: "Таймаут"}"
                    )
                }
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
        private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
        private val getServerUrlUseCase: GetServerUrlUseCase,
        private val saveServerUrlUseCase: SaveServerUrlUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val settingsRepository: SettingsRepository,
        private val sessionDataStore: SessionDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                getCurrentUserUseCase,
                getSettingsUseCase,
                getLibraryStatsUseCase,
                getServerUrlUseCase,
                saveServerUrlUseCase,
                logoutUseCase,
                settingsRepository,
                sessionDataStore
            ) as T
        }
    }
}

