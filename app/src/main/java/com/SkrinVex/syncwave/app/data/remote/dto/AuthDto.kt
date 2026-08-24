package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthStatusDto(
    @SerializedName("needs_setup") val needsSetup: Boolean = false,
    @SerializedName("allow_registration") val allowRegistration: Boolean = false
)

data class AuthRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("is_admin") val isAdmin: Boolean = false,
    @SerializedName("storage_quota_bytes") val storageQuotaBytes: Long = 0L,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class ErrorResponseDto(
    @SerializedName("error") val error: String? = null
)
