package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SystemSettingsDto(
    @SerializedName("http_proxy") val httpProxy: String? = null,
    @SerializedName("audio_format") val audioFormat: String? = null,
    @SerializedName("audio_quality") val audioQuality: String? = null,
    @SerializedName("max_concurrent") val maxConcurrent: Int = 1,
    @SerializedName("allow_registration") val allowRegistration: Boolean = false,
    @SerializedName("global_storage_limit_bytes") val globalStorageLimitBytes: Long = 0L,
    @SerializedName("default_user_quota_bytes") val defaultUserQuotaBytes: Long = 0L,
    @SerializedName("has_cookies") val hasCookies: Boolean = false,
    @SerializedName("cookies_valid") val cookiesValid: Boolean = false,
    @SerializedName("cookies_status") val cookiesStatus: String? = null,
    @SerializedName("cookies_expires_at") val cookiesExpiresAt: String? = null,
    @SerializedName("cookies_error") val cookiesError: String? = null,
    @SerializedName("cookies_updated_at") val cookiesUpdatedAt: String? = null,
    @SerializedName("ytdlp_version") val ytdlpVersion: String? = null,
    @SerializedName("ffmpeg_version") val ffmpegVersion: String? = null,
    @SerializedName("storage_usage_bytes") val storageUsageBytes: Long = 0L,
    @SerializedName("database_size_bytes") val databaseSizeBytes: Long = 0L,
    @SerializedName("total_tracks_count") val totalTracksCount: Int = 0,
    @SerializedName("total_playlists_count") val totalPlaylistsCount: Int = 0,
    @SerializedName("user_storage_usage_bytes") val userStorageUsageBytes: Long = 0L,
    @SerializedName("user_storage_quota_bytes") val userStorageQuotaBytes: Long = 0L,
    @SerializedName("host_disk_total_bytes") val hostDiskTotalBytes: Long = 0L,
    @SerializedName("host_disk_used_bytes") val hostDiskUsedBytes: Long = 0L,
    @SerializedName("host_disk_free_bytes") val hostDiskFreeBytes: Long = 0L,
    @SerializedName("is_admin") val isAdmin: Boolean = false
)
