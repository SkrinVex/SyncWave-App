package com.SkrinVex.syncwave.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SkrinVex.syncwave.app.domain.model.Resource
import com.SkrinVex.syncwave.app.domain.usecase.auth.CheckAuthStatusUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetSavedSessionUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.GetServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.LoginUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SaveServerUrlUseCase
import com.SkrinVex.syncwave.app.domain.usecase.auth.SetupAdminUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthEvent {
    data object NavigateToMain : AuthEvent()
    data class ShowToast(val message: String) : AuthEvent()
}

class AuthViewModel(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val loginUseCase: LoginUseCase,
    private val setupAdminUseCase: SetupAdminUseCase,
    private val saveServerUrlUseCase: SaveServerUrlUseCase,
    private val getServerUrlUseCase: GetServerUrlUseCase,
    private val getSavedSessionUseCase: GetSavedSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        loadInitialServerUrlAndSession()
    }

    private fun loadInitialServerUrlAndSession() {
        viewModelScope.launch {
            val savedUrl = getServerUrlUseCase()
            _uiState.update { it.copy(serverUrl = savedUrl) }
            checkServerStatus(targetUrl = savedUrl, isInitialCheck = true)
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url, errorMessage = null) }
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleServerSettings() {
        _uiState.update { it.copy(isServerSettingsOpen = !it.isServerSettingsOpen) }
    }

    fun checkServerStatus(targetUrl: String? = null, isInitialCheck: Boolean = false) {
        val url = targetUrl ?: _uiState.value.serverUrl
        if (url.isBlank()) {
            if (!isInitialCheck) {
                _uiState.update { it.copy(errorMessage = "Введите URL адрес сервера") }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingServer = true, errorMessage = if (isInitialCheck) null else it.errorMessage) }
            saveServerUrlUseCase(url)

            when (val result = checkAuthStatusUseCase(url)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isCheckingServer = false,
                            isServerConnected = true,
                            needsSetup = result.data.needsSetup,
                            allowRegistration = result.data.allowRegistration,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isCheckingServer = false,
                            isServerConnected = false,
                            errorMessage = if (isInitialCheck) null else result.message
                        )
                    }
                }
                Resource.Loading -> {}
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val username = state.username.trim()
        val password = state.password

        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите имя пользователя") }
            return
        }
        if (username.length < 3) {
            _uiState.update { it.copy(errorMessage = "Имя пользователя должно содержать не менее 3 символов") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите пароль") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Пароль должен содержать не менее 6 символов") }
            return
        }

        viewModelScope.launch {
            saveServerUrlUseCase(state.serverUrl)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (state.needsSetup) {
                setupAdminUseCase(username, password)
            } else {
                loginUseCase(username, password)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, session = result.data) }
                    _events.emit(AuthEvent.NavigateToMain)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> {}
            }
        }
    }

    class Factory(
        private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
        private val loginUseCase: LoginUseCase,
        private val setupAdminUseCase: SetupAdminUseCase,
        private val saveServerUrlUseCase: SaveServerUrlUseCase,
        private val getServerUrlUseCase: GetServerUrlUseCase,
        private val getSavedSessionUseCase: GetSavedSessionUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(
                checkAuthStatusUseCase,
                loginUseCase,
                setupAdminUseCase,
                saveServerUrlUseCase,
                getServerUrlUseCase,
                getSavedSessionUseCase
            ) as T
        }
    }
}
