package com.SkrinVex.syncwave.app.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.SkrinVex.syncwave.app.MainActivity
import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@OptIn(UnstableApi::class)
class AudioPlayerManager(
    private val context: Context,
    private val sessionDataStore: SessionDataStore,
    private val trackRepository: TrackRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    var exoPlayer: ExoPlayer? = null
        private set
    var mediaSession: MediaSession? = null
        private set
    private var progressJob: Job? = null
    private var simpleCache: SimpleCache? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    val currentTrackIdFlow: Flow<String?> = _playerState
        .map { it.currentTrack?.id }
        .distinctUntilChanged()

    val isPlayingFlow: Flow<Boolean> = _playerState
        .map { it.isPlaying }
        .distinctUntilChanged()

    init {
        createNotificationChannel()
        setupCacheAndPlayer()
        observeAudioFocusSetting()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "syncwave_playback_channel"
            val channelName = "SyncWave Воспроизведение"
            val channelDesc = "Системное медиа-уведомление SyncWave"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDesc
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun setupCacheAndPlayer() {
        try {
            val cacheDir = File(context.cacheDir, "media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(500L * 1024 * 1024) // 500 MB Audio Cache
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        } catch (_: Exception) {}

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)

        val dataSourceFactory = if (simpleCache != null) {
            CacheDataSource.Factory()
                .setCache(simpleCache!!)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            httpDataSourceFactory
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val handleAudioFocus = sessionDataStore.isAudioFocusEnabledCached()

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, handleAudioFocus)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressUpdates()
                    startMediaServiceSafely()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.update {
                            it.copy(
                                isBuffering = true,
                                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                            )
                        }
                    }
                    Player.STATE_READY -> {
                        _playerState.update {
                            it.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L),
                                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L)
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        handleTrackEnded()
                    }
                    Player.STATE_IDLE -> {
                        _playerState.update { it.copy(isBuffering = false) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _playerState.update { it.copy(isBuffering = false, isPlaying = false) }
            }
        })

        exoPlayer = player

        try {
            val sessionIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                sessionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val forwardingPlayer = object : ForwardingPlayer(player) {
                override fun getAvailableCommands(): Player.Commands {
                    return super.getAvailableCommands().buildUpon()
                        .add(COMMAND_SEEK_TO_NEXT)
                        .add(COMMAND_SEEK_TO_PREVIOUS)
                        .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                        .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                        .add(COMMAND_PLAY_PAUSE)
                        .build()
                }

                override fun isCommandAvailable(command: Int): Boolean {
                    return when (command) {
                        COMMAND_SEEK_TO_NEXT,
                        COMMAND_SEEK_TO_PREVIOUS,
                        COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                        COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                        COMMAND_PLAY_PAUSE -> true
                        else -> super.isCommandAvailable(command)
                    }
                }

                override fun seekToNext() {
                    this@AudioPlayerManager.playNext()
                }

                override fun seekToNextMediaItem() {
                    this@AudioPlayerManager.playNext()
                }

                override fun seekToPrevious() {
                    this@AudioPlayerManager.playPrevious()
                }

                override fun seekToPreviousMediaItem() {
                    this@AudioPlayerManager.playPrevious()
                }
            }

            mediaSession = MediaSession.Builder(context, forwardingPlayer)
                .setId("SyncWaveAudioSession")
                .setSessionActivity(pendingIntent)
                .build()
        } catch (_: Exception) {}
    }

    private fun observeAudioFocusSetting() {
        scope.launch {
            sessionDataStore.audioFocusEnabledFlow.collectLatest { enabled ->
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                exoPlayer?.setAudioAttributes(audioAttributes, enabled)
            }
        }
    }

    fun startMediaServiceSafely() {
        try {
            val intent = Intent(context, SyncWaveMediaService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    fun playTrackList(tracks: List<Track>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return

        val effectiveList = if (shuffle) tracks.shuffled() else tracks
        val effectiveIndex = if (shuffle) 0 else startIndex.coerceIn(0, tracks.lastIndex)

        _playerState.update {
            it.copy(
                queue = effectiveList,
                currentIndex = effectiveIndex,
                isShuffle = shuffle
            )
        }

        playTrack(effectiveList[effectiveIndex])
    }

    fun playTrack(track: Track, customQueue: List<Track>? = null) {
        val token = sessionDataStore.getTokenCached() ?: ""
        val streamUrl = trackRepository.getStreamUrl(track.id, token)
        val coverUrl = trackRepository.getCoverUrl(track.id, token)

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist.ifBlank { "Unknown Artist" })
            .setAlbumTitle(track.album.ifBlank { "SyncWave" })
            .setArtworkUri(Uri.parse(coverUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(Uri.parse(streamUrl))
            .setMediaMetadata(mediaMetadata)
            .build()

        val queueToSet = customQueue ?: _playerState.value.queue.ifEmpty { listOf(track) }
        val idx = queueToSet.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        _playerState.update {
            it.copy(
                currentTrack = track,
                queue = queueToSet,
                currentIndex = idx,
                currentPositionMs = 0L,
                bufferedPositionMs = 0L,
                durationMs = (track.duration * 1000L).coerceAtLeast(0L),
                isBuffering = true
            )
        }

        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }

        startMediaServiceSafely()
    }

    fun skipToQueueItem(index: Int) {
        val queue = _playerState.value.queue
        if (index in queue.indices) {
            _playerState.update { it.copy(currentIndex = index) }
            playTrack(queue[index])
        }
    }

    fun removeFromQueue(index: Int) {
        val state = _playerState.value
        if (index !in state.queue.indices) return

        val newQueue = state.queue.toMutableList()
        newQueue.removeAt(index)

        if (newQueue.isEmpty()) {
            stopPlayback()
            return
        }

        val newIndex = when {
            index < state.currentIndex -> state.currentIndex - 1
            index == state.currentIndex -> {
                val nextIdx = index.coerceAtMost(newQueue.lastIndex)
                playTrack(newQueue[nextIdx])
                nextIdx
            }
            else -> state.currentIndex
        }

        _playerState.update {
            it.copy(queue = newQueue, currentIndex = newIndex)
        }
    }

    fun reshuffleQueue() {
        val state = _playerState.value
        val curTrack = state.currentTrack ?: return
        val remaining = (state.queue - curTrack).shuffled()
        val newQueue = listOf(curTrack) + remaining

        _playerState.update {
            it.copy(
                queue = newQueue,
                currentIndex = 0,
                isShuffle = true
            )
        }
    }

    fun clearQueue() {
        val state = _playerState.value
        val curTrack = state.currentTrack ?: return
        _playerState.update {
            it.copy(
                queue = listOf(curTrack),
                currentIndex = 0
            )
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
            startMediaServiceSafely()
        }
    }

    fun playNext() {
        val state = _playerState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        var nextIndex = state.currentIndex + 1
        if (nextIndex >= queue.size) {
            if (state.repeatMode == RepeatMode.ALL) {
                nextIndex = 0
            } else {
                return
            }
        }

        _playerState.update { it.copy(currentIndex = nextIndex) }
        playTrack(queue[nextIndex])
    }

    fun playPrevious() {
        val state = _playerState.value
        val queue = state.queue
        if (queue.isEmpty()) return

        var prevIndex = state.currentIndex - 1
        if (prevIndex < 0) {
            if (state.repeatMode == RepeatMode.ALL) {
                prevIndex = queue.size - 1
            } else {
                // First track in queue, rewind to start
                exoPlayer?.seekTo(0)
                _playerState.update { it.copy(currentPositionMs = 0L) }
                return
            }
        }

        _playerState.update { it.copy(currentIndex = prevIndex) }
        playTrack(queue[prevIndex])
    }

    fun stopPlayback() {
        stopProgressUpdates()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        _playerState.update {
            it.copy(
                currentTrack = null,
                isPlaying = false,
                isBuffering = false,
                currentPositionMs = 0L,
                bufferedPositionMs = 0L,
                queue = emptyList(),
                currentIndex = -1
            )
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun toggleShuffle() {
        val currentShuffle = _playerState.value.isShuffle
        val nextShuffle = !currentShuffle
        _playerState.update { state ->
            val curTrack = state.currentTrack
            val newQueue = if (nextShuffle) {
                if (curTrack != null) {
                    listOf(curTrack) + (state.queue - curTrack).shuffled()
                } else {
                    state.queue.shuffled()
                }
            } else {
                state.queue
            }
            state.copy(isShuffle = nextShuffle, queue = newQueue, currentIndex = 0)
        }
    }

    fun cycleRepeatMode() {
        val nextMode = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playerState.update { it.copy(repeatMode = nextMode) }
    }

    private fun handleTrackEnded() {
        val state = _playerState.value
        if (state.repeatMode == RepeatMode.ONE) {
            exoPlayer?.seekTo(0)
            exoPlayer?.play()
        } else {
            playNext()
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playerState.update {
                            it.copy(
                                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                                durationMs = player.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(250L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdates()
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        try {
            simpleCache?.release()
            simpleCache = null
        } catch (_: Exception) {}
    }
}
