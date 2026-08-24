package com.SkrinVex.syncwave.app.domain.model

data class SystemSettings(
    val httpProxy: String = "",
    val audioFormat: String = "opus",
    val audioQuality: String = "best",
    val maxConcurrent: Int = 1,
    val allowRegistration: Boolean = false,
    val globalStorageLimitBytes: Long = 0L,
    val defaultUserQuotaBytes: Long = 0L,
    val hasCookies: Boolean = false,
    val cookiesValid: Boolean = false,
    val cookiesStatus: String = "missing",
    val ytdlpVersion: String = "",
    val ffmpegVersion: String = "",
    val storageUsageBytes: Long = 0L,
    val databaseSizeBytes: Long = 0L,
    val totalTracksCount: Int = 0,
    val totalPlaylistsCount: Int = 0,
    val userStorageUsageBytes: Long = 0L,
    val userStorageQuotaBytes: Long = 0L,
    val isAdmin: Boolean = false
) {
    val formattedUserStorage: String
        get() {
            val mb = userStorageUsageBytes.toDouble() / (1024 * 1024)
            return if (mb > 1024) {
                String.format("%.2f GB", mb / 1024)
            } else {
                String.format("%.1f MB", mb)
            }
        }

    val formattedUserQuota: String
        get() {
            if (userStorageQuotaBytes <= 0) return "Безлимитно"
            val gb = userStorageQuotaBytes.toDouble() / (1024 * 1024 * 1024)
            return String.format("%.1f GB", gb)
        }

    val quotaUsageFraction: Float
        get() {
            if (userStorageQuotaBytes <= 0) return 0f
            return (userStorageUsageBytes.toFloat() / userStorageQuotaBytes.toFloat()).coerceIn(0f, 1f)
        }
}
