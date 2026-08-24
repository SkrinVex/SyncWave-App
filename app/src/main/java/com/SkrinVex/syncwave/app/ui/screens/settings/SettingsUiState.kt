package com.SkrinVex.syncwave.app.ui.screens.settings

import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.model.TrackStats
import com.SkrinVex.syncwave.app.domain.model.User

data class SettingsUiState(
    val user: User? = null,
    val serverUrl: String = "",
    val settings: SystemSettings = SystemSettings(),
    val stats: TrackStats = TrackStats(),
    val isLoading: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val isEditServerUrlModalOpen: Boolean = false,
    val newServerUrl: String = "",
    val errorMessage: String? = null
)
