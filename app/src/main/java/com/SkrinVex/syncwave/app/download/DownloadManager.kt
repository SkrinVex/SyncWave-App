package com.SkrinVex.syncwave.app.download

import android.content.Context
import com.SkrinVex.syncwave.app.data.local.DownloadStorage
import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.domain.model.DownloadStatus
import com.SkrinVex.syncwave.app.domain.model.DownloadTask
import com.SkrinVex.syncwave.app.domain.model.DownloadedTrack
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadManager(
    private val context: Context,
    private val downloadStorage: DownloadStorage,
    private val sessionDataStore: SessionDataStore,
    private val trackRepository: TrackRepository,
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    private val _downloadedTracks = MutableStateFlow<List<DownloadedTrack>>(emptyList())
    val downloadedTracks: StateFlow<List<DownloadedTrack>> = _downloadedTracks.asStateFlow()

    private val _isCancelled = MutableStateFlow(false)
    val isCancelled: StateFlow<Boolean> = _isCancelled.asStateFlow()

    private val _cancelledSavedCount = MutableStateFlow(0)
    val cancelledSavedCount: StateFlow<Int> = _cancelledSavedCount.asStateFlow()

    val downloadedTrackIds: StateFlow<Set<String>> = _downloadedTracks
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    val isDownloading: StateFlow<Boolean> = _tasks
        .map { list -> list.any { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING } }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val overallProgress: StateFlow<Int> = _tasks
        .map { list ->
            if (list.isEmpty()) 0
            else {
                val sum = list.sumOf { it.progress }
                (sum / list.size).coerceIn(0, 100)
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    val totalStorageBytes: StateFlow<Long> = _downloadedTracks
        .map { list -> list.sumOf { it.sizeBytes } }
        .stateIn(scope, SharingStarted.Eagerly, 0L)

    private var activeJob: Job? = null
    private val runningTrackJobs = ConcurrentHashMap<String, Job>()

    init {
        refreshDownloadedTracks()
    }

    fun refreshDownloadedTracks() {
        val all = downloadStorage.getAllTracks()
        _downloadedTracks.value = all
    }

    fun isDownloaded(trackId: String): Boolean {
        return downloadStorage.isDownloaded(trackId)
    }

    fun getLocalAudioFile(trackId: String): File? {
        return downloadStorage.getLocalAudioFile(trackId)
    }

    fun getLocalCoverFile(trackId: String): File? {
        return downloadStorage.getLocalCoverFile(trackId)
    }

    fun enqueueDownload(track: Track) {
        if (downloadStorage.isDownloaded(track.id)) return

        val existing = _tasks.value.find { it.trackId == track.id }
        if (existing != null && (existing.status == DownloadStatus.PENDING || existing.status == DownloadStatus.DOWNLOADING)) {
            return
        }

        val estimatedBytes = when {
            track.fileSize > 1024L -> track.fileSize
            track.duration > 0 -> (track.duration * (track.bitrate.takeIf { it > 0 } ?: 160) * 1000L) / 8L
            else -> 0L
        }

        val task = DownloadTask(
            trackId = track.id,
            title = track.title,
            artist = track.artist.ifBlank { "Unknown Artist" },
            album = track.album,
            duration = track.duration,
            format = track.format,
            totalBytes = estimatedBytes,
            progress = 0,
            status = DownloadStatus.PENDING
        )

        _tasks.update { current ->
            val filtered = current.filter { it.trackId != track.id }
            filtered + task
        }

        startProcessingQueue()
    }

    fun enqueueDownloads(tracks: List<Track>) {
        val tracksToEnqueue = tracks.filter { !downloadStorage.isDownloaded(it.id) }
        if (tracksToEnqueue.isEmpty()) return

        val newTasks = mutableListOf<DownloadTask>()
        val currentTasks = _tasks.value

        for (track in tracksToEnqueue) {
            val existing = currentTasks.find { it.trackId == track.id }
            if (existing != null && (existing.status == DownloadStatus.PENDING || existing.status == DownloadStatus.DOWNLOADING)) {
                continue
            }

            val estimatedBytes = when {
                track.fileSize > 1024L -> track.fileSize
                track.duration > 0 -> (track.duration * (track.bitrate.takeIf { it > 0 } ?: 160) * 1000L) / 8L
                else -> 0L
            }

            newTasks.add(
                DownloadTask(
                    trackId = track.id,
                    title = track.title,
                    artist = track.artist.ifBlank { "Unknown Artist" },
                    album = track.album,
                    duration = track.duration,
                    format = track.format,
                    totalBytes = estimatedBytes,
                    progress = 0,
                    status = DownloadStatus.PENDING
                )
            )
        }

        if (newTasks.isEmpty()) return

        _tasks.update { current ->
            val existingIds = newTasks.map { it.trackId }.toSet()
            val filtered = current.filter { it.trackId !in existingIds }
            filtered + newTasks
        }

        startProcessingQueue()
    }

    private fun startProcessingQueue() {
        _isCancelled.value = false
        DownloadForegroundService.start(context)
        if (activeJob == null || activeJob?.isCompleted == true) {
            activeJob = scope.launch {
                processQueue()
            }
        }
    }

    private suspend fun processQueue() {
        while (true) {
            val pendingTask = _tasks.value.firstOrNull { it.status == DownloadStatus.PENDING } ?: break
            val trackJob = scope.launch {
                downloadSingleTrack(pendingTask)
            }
            runningTrackJobs[pendingTask.trackId] = trackJob
            trackJob.join()
            runningTrackJobs.remove(pendingTask.trackId)
        }
    }

    private suspend fun downloadSingleTrack(task: DownloadTask) = withContext(Dispatchers.IO) {
        val trackId = task.trackId
        updateTask(trackId) { it.copy(status = DownloadStatus.DOWNLOADING, progress = 0) }

        val token = sessionDataStore.getTokenCached() ?: sessionDataStore.getToken() ?: ""
        val serverUrl = sessionDataStore.getServerUrlCached().ifBlank { sessionDataStore.getServerUrl() }.trimEnd('/')
        val downloadUrl = "$serverUrl/api/v1/tracks/$trackId/download?token=$token"
        val coverUrl = "$serverUrl/api/v1/tracks/$trackId/cover?token=$token"

        val format = task.format.ifBlank { "opus" }
        val targetAudioFile = downloadStorage.getTargetAudioFile(trackId, format)
        val tempAudioFile = File(downloadStorage.getTracksDir(), "tmp_${trackId}_${System.currentTimeMillis()}.tmp")
        val targetCoverFile = downloadStorage.getTargetCoverFile(trackId)
        val tempCoverFile = File(downloadStorage.getCoversDir(), "tmp_cov_${trackId}_${System.currentTimeMillis()}.tmp")

        try {
            // 1. Download Audio stream with identity encoding to preserve Content-Length
            val audioRequest = Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept-Encoding", "identity")
                .build()

            val audioResponse = okHttpClient.newCall(audioRequest).execute()
            if (!audioResponse.isSuccessful || audioResponse.body == null) {
                val errMsg = "HTTP ${audioResponse.code}"
                updateTask(trackId) { it.copy(status = DownloadStatus.ERROR, progress = 100, errorMessage = errMsg) }
                return@withContext
            }

            val body = audioResponse.body!!
            val rawContentLength = body.contentLength()
            val totalBytes = when {
                rawContentLength > 1024L -> rawContentLength
                task.totalBytes > 1024L -> task.totalBytes
                task.duration > 0 -> (task.duration * 160L * 1000L) / 8L
                else -> 0L
            }

            var downloadedBytes = 0L
            var lastBytes = 0L
            var lastReportedTime = System.currentTimeMillis()
            var lastSpeedCalcTime = System.currentTimeMillis()
            var currentSpeed = 0L

            body.byteStream().use { input ->
                BufferedOutputStream(FileOutputStream(tempAudioFile), 64 * 1024).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val now = System.currentTimeMillis()
                        val timeDiff = now - lastReportedTime

                        if (timeDiff >= 300) {
                            val speedTimeDiff = now - lastSpeedCalcTime
                            if (speedTimeDiff >= 600) {
                                val bytesDiff = downloadedBytes - lastBytes
                                currentSpeed = ((bytesDiff.toDouble() / speedTimeDiff.toDouble()) * 1000).toLong().coerceAtLeast(0L)
                                lastBytes = downloadedBytes
                                lastSpeedCalcTime = now
                            }

                            val pct = if (totalBytes > 1024L) {
                                ((downloadedBytes.toDouble() / totalBytes.toDouble()) * 95).toInt().coerceIn(0, 95)
                            } else {
                                0
                            }

                            val remainingBytes = (totalBytes - downloadedBytes).coerceAtLeast(0L)
                            val etaSec = if (currentSpeed > 0 && remainingBytes > 0 && totalBytes > 1024L) remainingBytes / currentSpeed else 0L

                            updateTask(trackId) {
                                it.copy(
                                    progress = pct,
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                    speedBytesPerSec = currentSpeed,
                                    etaSeconds = etaSec
                                )
                            }
                            lastReportedTime = now
                        }
                    }
                    output.flush()
                }
            }

            // Integrity and Corrupted File Checks
            if (!tempAudioFile.exists() || tempAudioFile.length() < 1024L) {
                tempAudioFile.delete()
                updateTask(trackId) { it.copy(status = DownloadStatus.ERROR, progress = 100, errorMessage = "Некорректный аудиофайл") }
                return@withContext
            }

            if (rawContentLength > 1024L && tempAudioFile.length() < rawContentLength) {
                tempAudioFile.delete()
                updateTask(trackId) { it.copy(status = DownloadStatus.ERROR, progress = 100, errorMessage = "Загрузка не завершена") }
                return@withContext
            }

            // Atomically replace target audio file
            if (targetAudioFile.exists()) targetAudioFile.delete()
            tempAudioFile.renameTo(targetAudioFile)

            // 2. Download Cover art (optional, best effort with short timeout to prevent CDN blocking)
            var savedCoverPath: String? = null
            try {
                val coverRequest = Request.Builder()
                    .url(coverUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val coverClient = okHttpClient.newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build()
                val coverResponse = coverClient.newCall(coverRequest).execute()
                if (coverResponse.isSuccessful && coverResponse.body != null) {
                    coverResponse.body!!.byteStream().use { cInput ->
                        FileOutputStream(tempCoverFile).use { cOutput ->
                            cInput.copyTo(cOutput)
                        }
                    }
                    if (tempCoverFile.length() > 0) {
                        if (targetCoverFile.exists()) targetCoverFile.delete()
                        tempCoverFile.renameTo(targetCoverFile)
                        savedCoverPath = targetCoverFile.absolutePath
                    } else {
                        tempCoverFile.delete()
                    }
                }
            } catch (_: Exception) {
                tempCoverFile.delete()
            }

            // 3. Register downloaded track
            val downloadedTrack = DownloadedTrack(
                id = trackId,
                title = task.title,
                artist = task.artist,
                album = task.album,
                duration = task.duration,
                format = format,
                localFilePath = targetAudioFile.absolutePath,
                localCoverPath = savedCoverPath,
                sizeBytes = targetAudioFile.length(),
                downloadedAt = System.currentTimeMillis()
            )

            downloadStorage.saveDownloadedTrack(downloadedTrack)
            refreshDownloadedTracks()

            updateTask(trackId) {
                it.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 100,
                    downloadedBytes = targetAudioFile.length(),
                    totalBytes = targetAudioFile.length(),
                    speedBytesPerSec = 0L,
                    etaSeconds = 0L
                )
            }
        } catch (e: Exception) {
            tempAudioFile.delete()
            tempCoverFile.delete()
            if (e is CancellationException) {
                updateTask(trackId) { it.copy(status = DownloadStatus.CANCELLED, progress = 0, errorMessage = "Отменено пользователем") }
            } else {
                val err = e.localizedMessage ?: "Ошибка скачивания"
                updateTask(trackId) { it.copy(status = DownloadStatus.ERROR, progress = 100, errorMessage = err) }
            }
        }
    }

    private fun updateTask(trackId: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { list ->
            list.map { if (it.trackId == trackId) transform(it) else it }
        }
    }

    fun cancelDownload(trackId: String) {
        runningTrackJobs[trackId]?.cancel()
        _tasks.update { list -> list.filter { it.trackId != trackId } }
    }

    fun cancelAll() {
        runningTrackJobs.values.forEach { it.cancel() }
        runningTrackJobs.clear()
        activeJob?.cancel()
        activeJob = null

        _isCancelled.value = true
        _cancelledSavedCount.value = _downloadedTracks.value.size

        _tasks.update { list ->
            list.filter { it.status == DownloadStatus.COMPLETED }
        }

        DownloadForegroundService.stop(context)
    }

    fun clearCompleted() {
        _tasks.update { list ->
            list.filter { it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING }
        }
    }

    fun deleteDownloadedTrack(trackId: String): Boolean {
        cancelDownload(trackId)
        val success = downloadStorage.removeTrack(trackId)
        refreshDownloadedTracks()
        return success
    }

    fun deleteAllDownloadedTracks(): Boolean {
        cancelAll()
        val success = downloadStorage.clearAll()
        refreshDownloadedTracks()
        return success
    }

    /**
     * Synchronizes local cache with server tracks:
     * 1. Auto-deletes local tracks that are no longer on the server (if enabled in settings)
     * 2. Auto-downloads missing tracks from server (if enabled in settings)
     */
    fun syncWithServerTracks(serverTracks: List<Track>) {
        if (serverTracks.isEmpty()) return

        scope.launch {
            val serverIds = serverTracks.map { it.id }.toSet()

            // 1. Check auto-delete orphaned downloads
            val autoDeleteEnabled = sessionDataStore.isAutoDeleteOrphanedDownloadsCached()
            if (autoDeleteEnabled) {
                val localTracks = downloadStorage.getAllTracks()
                for (localTrack in localTracks) {
                    if (localTrack.id !in serverIds) {
                        downloadStorage.removeTrack(localTrack.id)
                    }
                }
                refreshDownloadedTracks()
            }

            // 2. Check auto-download missing tracks
            val autoDownloadEnabled = sessionDataStore.isAutoDownloadTracksCached()
            if (autoDownloadEnabled) {
                val missingTracks = serverTracks.filter { !downloadStorage.isDownloaded(it.id) }
                if (missingTracks.isNotEmpty()) {
                    enqueueDownloads(missingTracks)
                }
            }
        }
    }
}
