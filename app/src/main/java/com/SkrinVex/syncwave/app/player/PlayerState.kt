package com.SkrinVex.syncwave.app.player

import com.SkrinVex.syncwave.app.domain.model.Track

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    val progressFraction: Float
        get() {
            if (durationMs <= 0L) return 0f
            return (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val bufferedFraction: Float
        get() {
            if (durationMs <= 0L) return 0f
            return (bufferedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

    val formattedPosition: String
        get() = formatTime(currentPositionMs)

    val formattedDuration: String
        get() = formatTime(durationMs)

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%d:%02d", m, s)
        }
    }
}
