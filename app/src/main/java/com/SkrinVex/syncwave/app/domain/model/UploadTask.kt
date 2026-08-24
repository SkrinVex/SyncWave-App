package com.SkrinVex.syncwave.app.domain.model

import android.net.Uri
import java.util.Locale

enum class UploadStatus {
    PENDING,
    UPLOADING,
    PROCESSING,
    DONE,
    ERROR
}

data class UploadTask(
    val id: String,
    val uri: Uri,
    val name: String,
    val size: Long,
    val playlistId: String = "",
    val progress: Int = 0, // 0..100
    val status: UploadStatus = UploadStatus.PENDING,
    val errorMessage: String = ""
) {
    val formattedSize: String
        get() = formatBytes(size)
}

