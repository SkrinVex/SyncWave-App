package com.SkrinVex.syncwave.app.domain.model

enum class TrackStatus(val value: String) {
    QUEUED("queued"),
    DOWNLOADING("downloading"),
    READY("ready"),
    FAILED("failed");

    companion object {
        fun fromString(value: String): TrackStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: READY
        }
    }
}

data class Track(
    val id: String,
    val youtubeId: String = "",
    val playlistId: String? = null,
    val userId: String? = null,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Int = 0, // in seconds
    val filePath: String? = null,
    val coverPath: String? = null,
    val fileSize: Long = 0L,
    val format: String = "opus",
    val bitrate: Int = 160,
    val status: TrackStatus = TrackStatus.READY,
    val errorMessage: String? = null,
    val downloadedAt: String? = null,
    val createdAt: String? = null
) {
    val formattedDuration: String
        get() {
            if (duration <= 0) return "0:00"
            val h = duration / 3600
            val m = (duration % 3600) / 60
            val s = duration % 60
            return if (h > 0) {
                String.format("%d:%02d:%02d", h, m, s)
            } else {
                String.format("%d:%02d", m, s)
            }
        }

    val formattedSize: String
        get() {
            if (fileSize <= 0) return ""
            val mb = fileSize.toDouble() / (1024 * 1024)
            return String.format("%.1f MB", mb)
        }
}
