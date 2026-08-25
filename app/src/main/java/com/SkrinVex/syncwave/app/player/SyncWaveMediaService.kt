package com.SkrinVex.syncwave.app.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import coil.Coil
import coil.request.ImageRequest
import com.SkrinVex.syncwave.app.MainActivity
import com.SkrinVex.syncwave.app.R
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class SyncWaveMediaService : MediaSessionService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var lastLoadedCoverUrl: String? = null
    private var cachedArtworkBitmap: Bitmap? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "syncwave_playback_channel"

        const val ACTION_PLAY_PAUSE = "com.SkrinVex.syncwave.ACTION_PLAY_PAUSE"
        const val ACTION_PREV = "com.SkrinVex.syncwave.ACTION_PREV"
        const val ACTION_NEXT = "com.SkrinVex.syncwave.ACTION_NEXT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        observePlayerState()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SyncWave Воспроизведение",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Системное медиа-уведомление SyncWave"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun observePlayerState() {
        val playerManager = (application as? SyncWaveApplication)?.container?.audioPlayerManager ?: return
        serviceScope.launch {
            playerManager.playerState.collectLatest { state ->
                val track = state.currentTrack
                if (track != null && (state.isPlaying || state.isBuffering || state.currentPositionMs > 0)) {
                    updateNotification(track, state.isPlaying, state.isBuffering)
                } else if (!state.isPlaying && state.currentPositionMs == 0L) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerManager = (application as? SyncWaveApplication)?.container?.audioPlayerManager
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerManager?.togglePlayPause()
            ACTION_PREV -> playerManager?.playPrevious()
            ACTION_NEXT -> playerManager?.playNext()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotification(track: Track, isPlaying: Boolean, isBuffering: Boolean) {
        val playerManager = (application as? SyncWaveApplication)?.container?.audioPlayerManager ?: return
        val session = playerManager.mediaSession ?: return

        val sessionIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SyncWaveMediaService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, SyncWaveMediaService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, SyncWaveMediaService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Пауза" else "Воспроизведение"

        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
            .setShowActionsInCompactView(0, 1, 2)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(track.title)
            .setContentText(track.artist.ifBlank { "SyncWave" })
            .setSubText(track.album.ifBlank { "YouTube Music" })
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_previous, "Предыдущий", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Следующий", nextPendingIntent)

        if (cachedArtworkBitmap != null) {
            builder.setLargeIcon(cachedArtworkBitmap)
        }

        val notification = builder.build()

        if (isPlaying) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            stopForeground(STOP_FOREGROUND_DETACH)
            val notificationManager = NotificationManagerCompat.from(this)
            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: SecurityException) {}
        }

        // Asynchronously load album artwork bitmap if needed
        val container = (application as? SyncWaveApplication)?.container
        val token = container?.sessionDataStore?.getTokenCached() ?: ""
        val coverModel = container?.trackRepository?.getCoverModel(track.id, token)

        val modelKey = coverModel?.toString()
        if (coverModel != null && modelKey != lastLoadedCoverUrl) {
            lastLoadedCoverUrl = modelKey
            val imageRequest = ImageRequest.Builder(this)
                .data(coverModel)
                .target { drawable ->
                    (drawable as? BitmapDrawable)?.bitmap?.let { bmp ->
                        cachedArtworkBitmap = bmp
                        builder.setLargeIcon(bmp)
                        try {
                            NotificationManagerCompat.from(this@SyncWaveMediaService).notify(NOTIFICATION_ID, builder.build())
                        } catch (_: SecurityException) {}
                    }
                }
                .build()
            Coil.imageLoader(this).enqueue(imageRequest)
        }

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        val playerManager = (application as? SyncWaveApplication)?.container?.audioPlayerManager
        return playerManager?.mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = (application as? SyncWaveApplication)?.container?.audioPlayerManager?.exoPlayer
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
