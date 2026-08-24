package com.SkrinVex.syncwave.app.domain.model

data class User(
    val id: String,
    val username: String,
    val isAdmin: Boolean = false,
    val storageQuotaBytes: Long = 0L,
    val createdAt: String? = null
)
