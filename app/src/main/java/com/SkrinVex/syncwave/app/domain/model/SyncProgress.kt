package com.SkrinVex.syncwave.app.domain.model

data class SyncProgress(
    val active: Boolean = false,
    val playlistId: String = "",
    val playlistTitle: String = "",
    val currentTrackIndex: Int = 0,
    val totalTracks: Int = 0,
    val currentTrackTitle: String = "",
    val currentTrackId: String = "",
    val trackPercentage: Double = 0.0,
    val percentage: Double = 0.0,
    val speed: String = "",
    val eta: String = "",
    val statusText: String = ""
)
