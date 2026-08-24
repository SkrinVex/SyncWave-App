package com.SkrinVex.syncwave.app.domain.model

data class TrackStats(
    val totalTracks: Int = 0,
    val readyTracks: Int = 0,
    val failedTracks: Int = 0,
    val totalStorageSize: Long = 0L, // in bytes
    val totalDuration: Long = 0L // in seconds
) {
    val formattedTotalDuration: String
        get() {
            if (totalDuration <= 0) return "0 мин"
            val hours = totalDuration / 3600
            val minutes = (totalDuration % 3600) / 60
            return if (hours > 0) {
                "${hours} ч ${minutes} мин"
            } else {
                "${minutes} мин"
            }
        }

    val formattedTotalStorageSize: String
        get() {
            if (totalStorageSize <= 0) return "0 MB"
            val gb = totalStorageSize.toDouble() / (1024 * 1024 * 1024)
            return if (gb >= 1.0) {
                String.format("%.2f GB", gb)
            } else {
                val mb = totalStorageSize.toDouble() / (1024 * 1024)
                String.format("%.1f MB", mb)
            }
        }
}
