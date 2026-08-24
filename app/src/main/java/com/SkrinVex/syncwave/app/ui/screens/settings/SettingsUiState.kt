package com.SkrinVex.syncwave.app.ui.screens.settings

import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.model.User

data class SettingsUiState(
    val user: User? = null,
    val serverUrl: String = "",
    val settings: SystemSettings = SystemSettings(),
    val isLoading: Boolean = false,
    val isEditServerUrlModalOpen: Boolean = false,
    val newServerUrl: String = "",
    val errorMessage: String? = null
)
