package com.SkrinVex.syncwave.app.domain.model

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class DownloadedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Int = 0,
    val format: String = "",
    val localFilePath: String,
    val localCoverPath: String? = null,
    val sizeBytes: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val youtubeId: String = ""
) {
    val formattedDuration: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes.toDouble() / (1024 * 1024)
            return if (mb >= 1024) {
                "%.2f ГБ".format(mb / 1024)
            } else {
                "%.1f МБ".format(mb)
            }
        }

    fun toTrack(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            format = format,
            filePath = localFilePath,
            coverPath = localCoverPath ?: "",
            status = TrackStatus.READY,
            fileSize = sizeBytes,
            youtubeId = youtubeId
        )
    }
}

data class DownloadTask(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Int = 0,
    val format: String = "",
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val errorMessage: String? = null,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L
) {
    val id: String
        get() = trackId
}

