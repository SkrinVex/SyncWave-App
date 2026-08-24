package com.SkrinVex.syncwave.app.data.remote.dto

import com.SkrinVex.syncwave.app.domain.model.Playlist
import com.SkrinVex.syncwave.app.domain.model.SyncLog
import com.SkrinVex.syncwave.app.domain.model.SyncProgress
import com.SkrinVex.syncwave.app.domain.model.SystemSettings
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.model.TrackStats
import com.SkrinVex.syncwave.app.domain.model.TrackStatus
import com.SkrinVex.syncwave.app.domain.model.User
import com.SkrinVex.syncwave.app.domain.repository.AuthStatus
import com.SkrinVex.syncwave.app.domain.repository.TrackListResult

fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    isAdmin = isAdmin,
    storageQuotaBytes = storageQuotaBytes,
    createdAt = createdAt
)

fun AuthStatusDto.toDomain(): AuthStatus = AuthStatus(
    needsSetup = needsSetup,
    allowRegistration = allowRegistration
)

fun TrackDto.toDomain(): Track = Track(
    id = id,
    youtubeId = youtubeId ?: "",
    playlistId = playlistId,
    userId = userId,
    title = title,
    artist = artist,
    album = album ?: "",
    duration = duration,
    filePath = filePath,
    coverPath = coverPath,
    fileSize = fileSize,
    format = format ?: "opus",
    bitrate = bitrate,
    status = TrackStatus.fromString(status ?: "ready"),
    errorMessage = errorMessage,
    downloadedAt = downloadedAt,
    createdAt = createdAt
)

fun TrackListResponseDto.toDomain(): TrackListResult = TrackListResult(
    tracks = tracks?.map { it.toDomain() } ?: emptyList(),
    total = total,
    page = page,
    pageSize = pageSize,
    totalPages = totalPages
)

fun TrackStatsDto.toDomain(): TrackStats = TrackStats(
    totalTracks = totalTracks,
    readyTracks = readyTracks,
    failedTracks = failedTracks,
    totalStorageSize = totalStorageSize,
    totalDuration = totalDuration
)

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id,
    userId = userId,
    title = title,
    youtubeId = youtubeId,
    autoSync = autoSync,
    syncIntervalMinutes = syncIntervalMinutes,
    lastSyncedAt = lastSyncedAt,
    status = status ?: "idle",
    trackCount = trackCount,
    errorMessage = errorMessage,
    createdAt = createdAt
)

fun SyncProgressDto.toDomain(): SyncProgress = SyncProgress(
    active = active,
    playlistId = playlistId ?: "",
    playlistTitle = playlistTitle ?: "",
    currentTrackIndex = currentTrackIndex,
    totalTracks = totalTracks,
    currentTrackTitle = currentTrackTitle ?: "",
    currentTrackId = currentTrackId ?: "",
    trackPercentage = trackPercentage,
    percentage = percentage,
    speed = speed ?: "",
    eta = eta ?: "",
    statusText = statusText ?: ""
)

fun SyncLogDto.toDomain(): SyncLog = SyncLog(
    id = id,
    level = level ?: "info",
    message = message,
    createdAt = createdAt
)

fun SystemSettingsDto.toDomain(): SystemSettings = SystemSettings(
    httpProxy = httpProxy ?: "",
    audioFormat = audioFormat ?: "opus",
    audioQuality = audioQuality ?: "best",
    maxConcurrent = maxConcurrent,
    allowRegistration = allowRegistration,
    globalStorageLimitBytes = globalStorageLimitBytes,
    defaultUserQuotaBytes = defaultUserQuotaBytes,
    hasCookies = hasCookies,
    cookiesValid = cookiesValid,
    cookiesStatus = cookiesStatus ?: "missing",
    ytdlpVersion = ytdlpVersion ?: "",
    ffmpegVersion = ffmpegVersion ?: "",
    storageUsageBytes = storageUsageBytes,
    databaseSizeBytes = databaseSizeBytes,
    totalTracksCount = totalTracksCount,
    totalPlaylistsCount = totalPlaylistsCount,
    userStorageUsageBytes = userStorageUsageBytes,
    userStorageQuotaBytes = userStorageQuotaBytes,
    isAdmin = isAdmin
)
