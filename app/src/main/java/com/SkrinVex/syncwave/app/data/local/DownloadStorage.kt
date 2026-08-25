package com.SkrinVex.syncwave.app.data.local

import android.content.Context
import android.os.Environment
import com.SkrinVex.syncwave.app.domain.model.DownloadedTrack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DownloadStorage(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val tracksDir: File
    private val coversDir: File
    private val registryFile: File
    private val memoryRegistry = ConcurrentHashMap<String, DownloadedTrack>()

    init {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
        tracksDir = File(baseDir, "syncwave_tracks").apply { if (!exists()) mkdirs() }
        coversDir = File(baseDir, "syncwave_covers").apply { if (!exists()) mkdirs() }
        registryFile = File(context.filesDir, "downloaded_tracks_registry.json")
        loadRegistry()
    }

    @Synchronized
    private fun loadRegistry() {
        try {
            if (registryFile.exists()) {
                val json = registryFile.readText()
                if (json.isNotBlank()) {
                    val type = object : TypeToken<List<DownloadedTrack>>() {}.type
                    val list: List<DownloadedTrack> = gson.fromJson(json, type) ?: emptyList()
                    memoryRegistry.clear()
                    var changed = false
                    for (item in list) {
                        // Verify the audio file still exists on disk
                        val audioFile = File(item.localFilePath)
                        if (audioFile.exists() && audioFile.length() > 0) {
                            var currentCover = item.localCoverPath
                            if (currentCover == null || !File(currentCover).exists() || File(currentCover).length() == 0L) {
                                val targetCover = getTargetCoverFile(item.id)
                                if (targetCover.exists() && targetCover.length() > 0) {
                                    currentCover = targetCover.absolutePath
                                    changed = true
                                }
                            }
                            val updatedItem = if (currentCover != item.localCoverPath) item.copy(localCoverPath = currentCover) else item
                            memoryRegistry[item.id] = updatedItem
                        }
                    }
                    if (changed) {
                        persistRegistry()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    private fun persistRegistry() {
        try {
            val list = memoryRegistry.values.toList()
            val json = gson.toJson(list)
            registryFile.writeText(json)
        } catch (_: Exception) {}
    }

    fun getTracksDir(): File = tracksDir

    fun getCoversDir(): File = coversDir

    fun getTargetAudioFile(trackId: String, format: String): File {
        val cleanExt = format.trim().trimStart('.').ifBlank { "opus" }
        return File(tracksDir, "$trackId.$cleanExt")
    }

    fun getTargetCoverFile(trackId: String): File {
        return File(coversDir, "$trackId.jpg")
    }

    @Synchronized
    fun saveDownloadedTrack(downloadedTrack: DownloadedTrack) {
        val targetCover = getTargetCoverFile(downloadedTrack.id)
        val finalCoverPath = if (downloadedTrack.localCoverPath != null && File(downloadedTrack.localCoverPath).exists() && File(downloadedTrack.localCoverPath).length() > 0) {
            downloadedTrack.localCoverPath
        } else if (targetCover.exists() && targetCover.length() > 0) {
            targetCover.absolutePath
        } else {
            downloadedTrack.localCoverPath
        }
        memoryRegistry[downloadedTrack.id] = downloadedTrack.copy(localCoverPath = finalCoverPath)
        persistRegistry()
    }

    @Synchronized
    fun removeTrack(trackId: String): Boolean {
        val track = memoryRegistry.remove(trackId)
        if (track != null) {
            try {
                val audioFile = File(track.localFilePath)
                if (audioFile.exists()) audioFile.delete()
            } catch (_: Exception) {}

            try {
                track.localCoverPath?.let { path ->
                    val coverFile = File(path)
                    if (coverFile.exists()) coverFile.delete()
                }
                val defaultCover = getTargetCoverFile(trackId)
                if (defaultCover.exists()) defaultCover.delete()
            } catch (_: Exception) {}

            persistRegistry()
            return true
        }

        // Try deleting any orphaned files with that ID
        try {
            tracksDir.listFiles { file -> file.name.startsWith(trackId) }?.forEach { it.delete() }
            coversDir.listFiles { file -> file.name.startsWith(trackId) }?.forEach { it.delete() }
        } catch (_: Exception) {}

        return false
    }

    fun getTrack(trackId: String): DownloadedTrack? {
        val track = memoryRegistry[trackId] ?: return null
        val file = File(track.localFilePath)
        if (!file.exists() || file.length() == 0L) {
            removeTrack(trackId)
            return null
        }
        return track
    }

    fun getAllTracks(): List<DownloadedTrack> {
        val validTracks = mutableListOf<DownloadedTrack>()
        val toRemove = mutableListOf<String>()

        for ((id, track) in memoryRegistry) {
            val file = File(track.localFilePath)
            if (file.exists() && file.length() > 0) {
                validTracks.add(track)
            } else {
                toRemove.add(id)
            }
        }

        if (toRemove.isNotEmpty()) {
            toRemove.forEach { removeTrack(it) }
        }

        return validTracks.sortedByDescending { it.downloadedAt }
    }

    fun isDownloaded(trackId: String): Boolean {
        val track = memoryRegistry[trackId] ?: return false
        val file = File(track.localFilePath)
        return file.exists() && file.length() > 0
    }

    fun getLocalAudioFile(trackId: String): File? {
        val track = memoryRegistry[trackId] ?: return null
        val file = File(track.localFilePath)
        return if (file.exists() && file.length() > 0) file else null
    }

    fun getLocalCoverFile(trackId: String): File? {
        val track = memoryRegistry[trackId]
        if (track?.localCoverPath != null) {
            val file = File(track.localCoverPath)
            if (file.exists() && file.length() > 0) return file
        }

        val targetCover = getTargetCoverFile(trackId)
        if (targetCover.exists() && targetCover.length() > 0) {
            if (track != null && track.localCoverPath != targetCover.absolutePath) {
                memoryRegistry[trackId] = track.copy(localCoverPath = targetCover.absolutePath)
                persistRegistry()
            }
            return targetCover
        }

        // Search for any other image extension in coversDir
        try {
            val matches = coversDir.listFiles { file -> file.name.startsWith(trackId) && file.length() > 0 }
            if (!matches.isNullOrEmpty()) {
                val found = matches.first()
                if (track != null) {
                    memoryRegistry[trackId] = track.copy(localCoverPath = found.absolutePath)
                    persistRegistry()
                }
                return found
            }
        } catch (_: Exception) {}

        return null
    }

    fun getCoverModel(trackId: String, fallbackUrl: String): Any {
        val localFile = getLocalCoverFile(trackId)
        return if (localFile != null && localFile.exists() && localFile.length() > 0) {
            localFile
        } else {
            fallbackUrl
        }
    }

    fun getTotalStorageBytes(): Long {
        return getAllTracks().sumOf { it.sizeBytes }
    }

    @Synchronized
    fun clearAll(): Boolean {
        try {
            memoryRegistry.clear()
            persistRegistry()
            tracksDir.listFiles()?.forEach { it.delete() }
            coversDir.listFiles()?.forEach { it.delete() }
            return true
        } catch (_: Exception) {
            return false
        }
    }
}


