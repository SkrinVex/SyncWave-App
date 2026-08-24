package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SyncProgressDto(
    @SerializedName("active") val active: Boolean = false,
    @SerializedName("playlist_id") val playlistId: String? = null,
    @SerializedName("playlist_title") val playlistTitle: String? = null,
    @SerializedName("current_track_index") val currentTrackIndex: Int = 0,
    @SerializedName("total_tracks") val totalTracks: Int = 0,
    @SerializedName("current_track_title") val currentTrackTitle: String? = null,
    @SerializedName("current_track_id") val currentTrackId: String? = null,
    @SerializedName("track_percentage") val trackPercentage: Double = 0.0,
    @SerializedName("percentage") val percentage: Double = 0.0,
    @SerializedName("speed") val speed: String? = null,
    @SerializedName("eta") val eta: String? = null,
    @SerializedName("status_text") val statusText: String? = null
)

data class SyncLogDto(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("level") val level: String? = "info",
    @SerializedName("message") val message: String,
    @SerializedName("created_at") val createdAt: String? = null
)
