package com.SkrinVex.syncwave.app.domain.model

data class SyncLog(
    val id: Long,
    val level: String = "info", // info, success, warn, error
    val message: String,
    val createdAt: String? = null
)
