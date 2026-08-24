package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("youtube_id") val youtubeId: String? = null,
    @SerializedName("playlist_id") val playlistId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album") val album: String? = null,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("cover_path") val coverPath: String? = null,
    @SerializedName("file_size") val fileSize: Long = 0L,
    @SerializedName("format") val format: String? = "opus",
    @SerializedName("bitrate") val bitrate: Int = 160,
    @SerializedName("status") val status: String? = "ready",
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("downloaded_at") val downloadedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class TrackListResponseDto(
    @SerializedName("tracks") val tracks: List<TrackDto>? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("page_size") val pageSize: Int = 50,
    @SerializedName("total_pages") val totalPages: Int = 1
)

data class TrackStatsDto(
    @SerializedName("total_tracks") val totalTracks: Int = 0,
    @SerializedName("ready_tracks") val readyTracks: Int = 0,
    @SerializedName("failed_tracks") val failedTracks: Int = 0,
    @SerializedName("total_storage_size") val totalStorageSize: Long = 0L,
    @SerializedName("total_duration") val totalDuration: Long = 0L
)

data class BatchDeleteRequestDto(
    @SerializedName("ids") val ids: List<String>
)
