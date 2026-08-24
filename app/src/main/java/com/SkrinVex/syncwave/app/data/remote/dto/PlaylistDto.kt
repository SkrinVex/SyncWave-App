package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlaylistDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("youtube_id") val youtubeId: String,
    @SerializedName("auto_sync") val autoSync: Boolean = true,
    @SerializedName("sync_interval_minutes") val syncIntervalMinutes: Int = 60,
    @SerializedName("last_synced_at") val lastSyncedAt: String? = null,
    @SerializedName("status") val status: String? = "idle",
    @SerializedName("track_count") val trackCount: Int = 0,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CreatePlaylistRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("url_or_id") val urlOrId: String,
    @SerializedName("auto_sync") val autoSync: Boolean = true,
    @SerializedName("sync_interval_minutes") val syncIntervalMinutes: Int = 60
)

data class UpdatePlaylistRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("auto_sync") val autoSync: Boolean = true,
    @SerializedName("sync_interval_minutes") val syncIntervalMinutes: Int = 60
)
