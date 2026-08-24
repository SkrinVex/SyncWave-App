package com.SkrinVex.syncwave.app.domain.model

data class AuthSession(
    val token: String,
    val user: User,
    val serverUrl: String
)
