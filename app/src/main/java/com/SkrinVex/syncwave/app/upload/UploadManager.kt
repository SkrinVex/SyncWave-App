package com.SkrinVex.syncwave.app.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.SkrinVex.syncwave.app.data.remote.api.SyncWaveApiService
import com.SkrinVex.syncwave.app.domain.model.UploadStatus
import com.SkrinVex.syncwave.app.domain.model.UploadTask
import com.SkrinVex.syncwave.app.domain.repository.PlaylistRepository
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class UploadManager(
    private val apiService: SyncWaveApiService,
    private val trackRepository: TrackRepository,
    private val playlistRepository: PlaylistRepository
) {
    companion object {
        val VALID_AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "m4a", "opus", "ogg", "wav", "aac", "wma"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _tasks = MutableStateFlow<List<UploadTask>>(emptyList())
    val tasks: StateFlow<List<UploadTask>> = _tasks.asStateFlow()

    private val _isMinimized = MutableStateFlow(false)
    val isMinimized: StateFlow<Boolean> = _isMinimized.asStateFlow()

    val isUploading: StateFlow<Boolean> = _tasks.map { list ->
        list.any { it.status == UploadStatus.PENDING || it.status == UploadStatus.UPLOADING || it.status == UploadStatus.PROCESSING }
    }.stateIn(scope, SharingStarted.Eagerly, false)

    val totalTasks: StateFlow<Int> = _tasks.map { it.size }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val completedTasks: StateFlow<Int> = _tasks.map { list ->
        list.count { it.status == UploadStatus.DONE }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    val failedTasks: StateFlow<Int> = _tasks.map { list ->
        list.count { it.status == UploadStatus.ERROR }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    val overallProgress: StateFlow<Int> = _tasks.map { list ->
        if (list.isEmpty()) 0
        else {
            val sum = list.sumOf { it.progress }
            (sum / list.size).coerceIn(0, 100)
        }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    fun setMinimized(minimized: Boolean) {
        _isMinimized.value = minimized
    }

    fun toggleMinimized() {
        _isMinimized.value = !_isMinimized.value
    }

    fun removeTask(taskId: String) {
        _tasks.update { list -> list.filter { it.id != taskId } }
    }

    fun clearCompleted() {
        _tasks.update { list ->
            list.filter { it.status == UploadStatus.PENDING || it.status == UploadStatus.UPLOADING || it.status == UploadStatus.PROCESSING }
        }
    }

    fun enqueueUploads(context: Context, uris: List<Uri>, playlistId: String = "") {
        if (uris.isEmpty()) return

        val newTasks = mutableListOf<UploadTask>()
        val contentResolver = context.contentResolver

        for (uri in uris) {
            var fileName = "audio_${System.currentTimeMillis()}"
            var fileSize = 0L

            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) fileName = name
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}

            val mimeType = contentResolver.getType(uri) ?: ""
            val ext = fileName.substringAfterLast('.', "").lowercase()

            val isInvalidFormat = ext !in VALID_AUDIO_EXTENSIONS ||
                    mimeType.startsWith("video/") ||
                    mimeType.startsWith("image/") ||
                    mimeType.startsWith("text/") ||
                    mimeType.startsWith("application/pdf") ||
                    mimeType.startsWith("application/zip")

            if (isInvalidFormat) {
                newTasks.add(
                    UploadTask(
                        id = UUID.randomUUID().toString(),
                        uri = uri,
                        name = fileName,
                        size = fileSize,
                        playlistId = playlistId,
                        progress = 100,
                        status = UploadStatus.ERROR,
                        errorMessage = "Недопустимый формат ($ext). Разрешена загрузка только аудио (.mp3, .m4a, .flac, .opus, .ogg, .wav, .aac, .wma)"
                    )
                )
            } else {
                newTasks.add(
                    UploadTask(
                        id = UUID.randomUUID().toString(),
                        uri = uri,
                        name = fileName,
                        size = fileSize,
                        playlistId = playlistId,
                        progress = 0,
                        status = UploadStatus.PENDING
                    )
                )
            }
        }

        _tasks.update { current -> current + newTasks }
        _isMinimized.value = false

        val validPendingTasks = newTasks.filter { it.status == UploadStatus.PENDING }
        if (validPendingTasks.isNotEmpty()) {
            UploadForegroundService.start(context)
            scope.launch {
                processUploads(context, validPendingTasks, playlistId)
            }
        }
    }

    private suspend fun processUploads(context: Context, tasksToProcess: List<UploadTask>, playlistId: String) {
        val batchSize = 2
        for (i in tasksToProcess.indices step batchSize) {
            val chunk = tasksToProcess.subList(i, minOf(i + batchSize, tasksToProcess.size))
            chunk.forEach { task ->
                uploadSingleFile(context, task, playlistId)
            }
        }

        // Refresh library and playlists after uploads
        try {
            trackRepository.getStats()
            playlistRepository.getPlaylists()
        } catch (_: Exception) {}
    }

    private suspend fun uploadSingleFile(context: Context, task: UploadTask, playlistId: String) = withContext(Dispatchers.IO) {
        updateTask(task.id) { it.copy(status = UploadStatus.UPLOADING, progress = 0) }

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(task.uri) ?: "audio/*"
        val mediaType = mimeType.toMediaTypeOrNull()

        try {
            var actualSize = task.size
            if (actualSize <= 0L) {
                contentResolver.openFileDescriptor(task.uri, "r")?.use { pfd ->
                    actualSize = pfd.statSize
                }
            }

            val progressRequestBody = ProgressRequestBody(
                contentResolver = contentResolver,
                uri = task.uri,
                contentType = mediaType,
                totalLength = actualSize,
                onProgress = { bytesWritten, totalBytes ->
                    val pct = if (totalBytes > 0) {
                        ((bytesWritten.toDouble() / totalBytes.toDouble()) * 90).toInt().coerceIn(0, 90)
                    } else 45

                    updateTask(task.id) { current ->
                        val newStatus = if (pct >= 90) UploadStatus.PROCESSING else UploadStatus.UPLOADING
                        current.copy(progress = pct, status = newStatus)
                    }
                }
            )

            val part = MultipartBody.Part.createFormData("files", task.name, progressRequestBody)
            val playlistIdBody = if (playlistId.isNotBlank()) {
                playlistId.toRequestBody("text/plain".toMediaTypeOrNull())
            } else null

            updateTask(task.id) { it.copy(status = UploadStatus.PROCESSING, progress = 90) }

            val response = apiService.uploadTracks(listOf(part), playlistIdBody)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                if (!result.errors.isNullOrEmpty() && (result.uploaded.isNullOrEmpty())) {
                    val err = result.errors.joinToString(", ")
                    updateTask(task.id) { it.copy(status = UploadStatus.ERROR, progress = 100, errorMessage = err) }
                } else {
                    updateTask(task.id) { it.copy(status = UploadStatus.DONE, progress = 100, errorMessage = "") }
                }
            } else {
                val err = "Ошибка сервера (HTTP ${response.code()})"
                updateTask(task.id) { it.copy(status = UploadStatus.ERROR, progress = 100, errorMessage = err) }
            }
        } catch (e: Exception) {
            val err = e.localizedMessage ?: "Сетевая ошибка при передаче файла"
            updateTask(task.id) { it.copy(status = UploadStatus.ERROR, progress = 100, errorMessage = err) }
        }
    }

    private fun updateTask(taskId: String, transform: (UploadTask) -> UploadTask) {
        _tasks.update { list ->
            list.map { if (it.id == taskId) transform(it) else it }
        }
    }
}
