package com.SkrinVex.syncwave.app.domain.model

data class Playlist(
    val id: String,
    val userId: String? = null,
    val title: String,
    val youtubeId: String,
    val autoSync: Boolean = true,
    val syncIntervalMinutes: Int = 60,
    val lastSyncedAt: String? = null,
    val status: String = "idle",
    val trackCount: Int = 0,
    val errorMessage: String? = null,
    val createdAt: String? = null
)
