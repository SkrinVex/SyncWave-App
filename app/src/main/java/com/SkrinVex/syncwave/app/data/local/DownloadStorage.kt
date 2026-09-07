package com.SkrinVex.syncwave.app.data.local

import android.content.Context
import android.os.Environment
import com.SkrinVex.syncwave.app.domain.model.DownloadedTrack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Registry of tracks downloaded to the device.
 *
 * Everything the UI touches while composing (cover lookups, "is this downloaded"
 * checks) is answered from memory only. Disk access happens on load, on writes and
 * inside [revalidate], which callers must run off the main thread.
 */
class DownloadStorage(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val tracksDir: File
    private val coversDir: File
    private val registryFile: File
    private val memoryRegistry = ConcurrentHashMap<String, DownloadedTrack>()

    /** trackId -> absolute cover path. Mirrors the registry so the UI never stats files. */
    private val coverPathCache = ConcurrentHashMap<String, String>()

    private val revalidating = AtomicBoolean(false)

    init {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
        tracksDir = File(baseDir, "syncwave_tracks").apply { if (!exists()) mkdirs() }
        coversDir = File(baseDir, "syncwave_covers").apply { if (!exists()) mkdirs() }
        registryFile = File(context.filesDir, "downloaded_tracks_registry.json")
        loadRegistry()
    }

    /**
     * Reads the registry file into memory. Deliberately does not stat every entry:
     * that was O(number of downloads) syscalls before the first frame could render.
     * Stale entries are pruned later by [revalidate].
     */
    @Synchronized
    private fun loadRegistry() {
        try {
            if (!registryFile.exists()) return
            val json = registryFile.readText()
            if (json.isBlank()) return
            val type = object : TypeToken<List<DownloadedTrack>>() {}.type
            val list: List<DownloadedTrack> = gson.fromJson(json, type) ?: emptyList()
            memoryRegistry.clear()
            coverPathCache.clear()
            for (item in list) {
                memoryRegistry[item.id] = item
                item.localCoverPath?.takeIf { it.isNotBlank() }?.let { coverPathCache[item.id] = it }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Prunes entries whose audio file vanished and rediscovers covers that were saved
     * under the default name. Touches the disk heavily - call from a background thread.
     */
    fun revalidate() {
        if (!revalidating.compareAndSet(false, true)) return
        try {
            var changed = false
            for ((id, item) in memoryRegistry) {
                val audioFile = File(item.localFilePath)
                if (!audioFile.exists() || audioFile.length() == 0L) {
                    memoryRegistry.remove(id)
                    coverPathCache.remove(id)
                    changed = true
                    continue
                }

                val declaredCover = item.localCoverPath
                val declaredCoverValid = declaredCover != null &&
                        File(declaredCover).let { it.exists() && it.length() > 0 }

                if (declaredCoverValid) {
                    coverPathCache[id] = declaredCover!!
                    continue
                }

                val resolved = findCoverOnDisk(id)
                if (resolved != null) {
                    memoryRegistry[id] = item.copy(localCoverPath = resolved)
                    coverPathCache[id] = resolved
                    changed = true
                } else if (declaredCover != null) {
                    memoryRegistry[id] = item.copy(localCoverPath = null)
                    coverPathCache.remove(id)
                    changed = true
                }
            }
            if (changed) persistRegistry()
        } catch (_: Exception) {
        } finally {
            revalidating.set(false)
        }
    }

    /** Looks for a cover file on disk. Performs I/O - never call while composing. */
    private fun findCoverOnDisk(trackId: String): String? {
        val target = getTargetCoverFile(trackId)
        if (target.exists() && target.length() > 0) return target.absolutePath
        return try {
            coversDir
                .listFiles { file -> file.name.startsWith(trackId) && file.length() > 0 }
                ?.firstOrNull()
                ?.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    private fun persistRegistry() {
        try {
            val list = memoryRegistry.values.toList()
            val json = gson.toJson(list)
            registryFile.writeText(json)
        } catch (_: Exception) {
        }
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
        val declared = downloadedTrack.localCoverPath
        val finalCoverPath = when {
            declared != null && File(declared).let { it.exists() && it.length() > 0 } -> declared
            else -> findCoverOnDisk(downloadedTrack.id) ?: declared
        }
        memoryRegistry[downloadedTrack.id] = downloadedTrack.copy(localCoverPath = finalCoverPath)
        if (finalCoverPath != null) {
            coverPathCache[downloadedTrack.id] = finalCoverPath
        } else {
            coverPathCache.remove(downloadedTrack.id)
        }
        persistRegistry()
    }

    @Synchronized
    fun removeTrack(trackId: String): Boolean {
        val track = memoryRegistry.remove(trackId)
        coverPathCache.remove(trackId)
        if (track != null) {
            try {
                val audioFile = File(track.localFilePath)
                if (audioFile.exists()) audioFile.delete()
            } catch (_: Exception) {
            }

            try {
                track.localCoverPath?.let { path ->
                    val coverFile = File(path)
                    if (coverFile.exists()) coverFile.delete()
                }
                val defaultCover = getTargetCoverFile(trackId)
                if (defaultCover.exists()) defaultCover.delete()
            } catch (_: Exception) {
            }

            persistRegistry()
            return true
        }

        // Try deleting any orphaned files with that ID
        try {
            tracksDir.listFiles { file -> file.name.startsWith(trackId) }?.forEach { it.delete() }
            coversDir.listFiles { file -> file.name.startsWith(trackId) }?.forEach { it.delete() }
        } catch (_: Exception) {
        }

        return false
    }

    /**
     * Removes several tracks in one pass, writing the registry once instead of once per
     * track. Deletes files - call from a background thread.
     */
    @Synchronized
    fun removeTracks(trackIds: Collection<String>): Int {
        if (trackIds.isEmpty()) return 0
        var removed = 0
        for (trackId in trackIds) {
            val track = memoryRegistry.remove(trackId)
            coverPathCache.remove(trackId)
            try {
                if (track != null) {
                    File(track.localFilePath).takeIf { it.exists() }?.delete()
                    track.localCoverPath?.let { path ->
                        File(path).takeIf { it.exists() }?.delete()
                    }
                    removed++
                }
                getTargetCoverFile(trackId).takeIf { it.exists() }?.delete()
            } catch (_: Exception) {
            }
        }
        persistRegistry()
        return removed
    }

    fun getTrack(trackId: String): DownloadedTrack? = memoryRegistry[trackId]

    /** Memory-only snapshot, newest first. Safe to call while composing. */
    fun getAllTracks(): List<DownloadedTrack> =
        memoryRegistry.values.sortedByDescending { it.downloadedAt }

    /** Memory-only. Reflects the registry, which [revalidate] keeps honest. */
    fun isDownloaded(trackId: String): Boolean = memoryRegistry.containsKey(trackId)

    fun getLocalAudioFile(trackId: String): File? {
        val track = memoryRegistry[trackId] ?: return null
        val file = File(track.localFilePath)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Cover file for a track, resolved from the in-memory cache. Falls back to a disk
     * search only when the cache has no entry, so call it off the main thread when a
     * miss is likely (downloads, cover sync).
     */
    fun getLocalCoverFile(trackId: String): File? {
        coverPathCache[trackId]?.let { cached ->
            val file = File(cached)
            if (file.exists() && file.length() > 0) return file
            coverPathCache.remove(trackId)
        }

        val resolved = findCoverOnDisk(trackId) ?: return null
        coverPathCache[trackId] = resolved
        memoryRegistry[trackId]?.let { track ->
            if (track.localCoverPath != resolved) {
                memoryRegistry[trackId] = track.copy(localCoverPath = resolved)
            }
        }
        return File(resolved)
    }

    /**
     * Image model for Coil: a local [File] when one is known, otherwise the remote URL.
     * Pure memory lookup so it is safe to call for every row of a scrolling list.
     */
    fun getCoverModel(trackId: String, fallbackUrl: String): Any {
        val cached = coverPathCache[trackId] ?: return fallbackUrl
        return File(cached)
    }

    fun getTotalStorageBytes(): Long = memoryRegistry.values.sumOf { it.sizeBytes }

    @Synchronized
    fun clearAll(): Boolean {
        try {
            memoryRegistry.clear()
            coverPathCache.clear()
            persistRegistry()
            tracksDir.listFiles()?.forEach { it.delete() }
            coversDir.listFiles()?.forEach { it.delete() }
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
