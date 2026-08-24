package com.SkrinVex.syncwave.app.player

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.SkrinVex.syncwave.app.SyncWaveApplication

@OptIn(UnstableApi::class)
class SyncWaveMediaService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val playerManager = (application as SyncWaveApplication).container.audioPlayerManager
        val player = playerManager.exoPlayer
        if (player != null) {
            mediaSession = MediaSession.Builder(this, player)
                .setId("SyncWaveMediaSession")
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
