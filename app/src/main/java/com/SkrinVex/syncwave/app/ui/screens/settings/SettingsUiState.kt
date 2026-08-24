package com.SkrinVex.syncwave.app.ui.screens.settings

import com.SkrinVex.syncwave.app.domain.model.DownloadedTrack
import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.model.TrackStats
import com.SkrinVex.syncwave.app.domain.model.User

data class SettingsUiState(
    val user: User? = null,
    val settings: SystemSettings = SystemSettings(),
    val stats: TrackStats = TrackStats(),
    val serverUrl: String = "",
    val isAudioFocusEnabled: Boolean = true,
    val isAutoDownloadEnabled: Boolean = true,
    val isAutoDeleteOrphanedEnabled: Boolean = true,
    val downloadedTracks: List<DownloadedTrack> = emptyList(),
    val downloadedTotalStorageFormatted: String = "0 МБ",
    val isDownloadsSheetOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: String? = null,
    val isEditServerUrlModalOpen: Boolean = false,
    val newServerUrl: String = "",
    val isGoogleAuthModalOpen: Boolean = false,
    val isUploadingCookies: Boolean = false,
    val cookiesOperationMessage: String? = null,
    val errorMessage: String? = null
)
