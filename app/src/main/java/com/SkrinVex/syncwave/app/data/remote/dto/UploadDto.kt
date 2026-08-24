package com.SkrinVex.syncwave.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadResultDto(
    @SerializedName("uploaded") val uploaded: List<TrackDto>? = null,
    @SerializedName("errors") val errors: List<String>? = null
)

