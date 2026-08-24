package com.SkrinVex.syncwave.app.domain.model

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    ERROR,
    CANCELLED
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
    val downloadedBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L
) {
    val id: String
        get() = trackId

    val formattedSpeed: String
        get() {
            if (speedBytesPerSec <= 0) return ""
            val kb = speedBytesPerSec / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                "%.1f МБ/с".format(mb)
            } else {
                "%.0f КБ/с".format(kb)
            }
        }

    val formattedEta: String
        get() {
            if (etaSeconds <= 0) return ""
            val m = etaSeconds / 60
            val s = etaSeconds % 60
            return if (m > 0) {
                "ETA: %dм %02dс".format(m, s)
            } else {
                "ETA: %dс".format(s)
            }
        }

    val downloadedMbFormatted: String
        get() {
            val dMb = downloadedBytes.toDouble() / (1024 * 1024)
            val tMb = totalBytes.toDouble() / (1024 * 1024)
            return if (totalBytes > 1024L) {
                "%.1f / %.1f МБ".format(dMb, tMb)
            } else if (downloadedBytes > 0) {
                "%.1f МБ".format(dMb)
            } else {
                "0 МБ"
            }
        }
}
