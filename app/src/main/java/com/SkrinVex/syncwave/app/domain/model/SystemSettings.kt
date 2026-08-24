package com.SkrinVex.syncwave.app.domain.model

import java.util.Locale

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
    val cookiesExpiresAt: String = "",
    val cookiesError: String = "",
    val cookiesUpdatedAt: String = "",
    val ytdlpVersion: String = "",
    val ffmpegVersion: String = "",
    val storageUsageBytes: Long = 0L,
    val databaseSizeBytes: Long = 0L,
    val totalTracksCount: Int = 0,
    val totalPlaylistsCount: Int = 0,
    val userStorageUsageBytes: Long = 0L,
    val userStorageQuotaBytes: Long = 0L,
    val hostDiskTotalBytes: Long = 0L,
    val hostDiskUsedBytes: Long = 0L,
    val hostDiskFreeBytes: Long = 0L,
    val isAdmin: Boolean = false
) {
    val isCookiesExpired: Boolean
        get() = cookiesStatus == "expired" || cookiesStatus == "invalid"

    val isCookiesExpiringSoon: Boolean
        get() = cookiesStatus == "expiring_soon"

    val isCookiesValid: Boolean
        get() = hasCookies && cookiesValid && cookiesStatus == "valid"

    val effectiveUserUsageBytes: Long
        get() = if (userStorageUsageBytes > 0) userStorageUsageBytes else storageUsageBytes

    val formattedUserStorage: String
        get() = formatBytes(effectiveUserUsageBytes)

    val formattedUserQuota: String
        get() = if (userStorageQuotaBytes <= 0) "Безлимитно" else formatBytes(userStorageQuotaBytes)

    val quotaUsageFraction: Float
        get() {
            if (userStorageQuotaBytes <= 0) return 0f
            return (effectiveUserUsageBytes.toFloat() / userStorageQuotaBytes.toFloat()).coerceIn(0f, 1f)
        }

    val quotaUsagePercent: Int
        get() {
            if (userStorageQuotaBytes <= 0) return 0
            return ((effectiveUserUsageBytes.toDouble() / userStorageQuotaBytes.toDouble()) * 100).toInt().coerceIn(0, 100)
        }

    val formattedDbSize: String
        get() = formatBytes(databaseSizeBytes)

    val formattedHostDiskFree: String
        get() = "${formatBytes(hostDiskFreeBytes)} свободно"
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val k = 1024.0
    val sizes = arrayOf("B", "KB", "MB", "GB", "TB")
    val i = (Math.log(bytes.toDouble()) / Math.log(k)).toInt().coerceIn(0, sizes.lastIndex)
    val value = bytes.toDouble() / Math.pow(k, i.toDouble())
    return String.format(Locale.US, "%.1f %s", value, sizes[i])
}
