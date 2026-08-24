package com.SkrinVex.syncwave.app.ui.screens.auth

import com.SkrinVex.syncwave.app.domain.model.AuthSession

data class AuthUiState(
    val serverUrl: String = "https://syncwave.skrinvex.com",
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isServerSettingsOpen: Boolean = false,
    val isCheckingServer: Boolean = false,
    val isServerConnected: Boolean = false,
    val needsSetup: Boolean = false,
    val allowRegistration: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val session: AuthSession? = null
)
