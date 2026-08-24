package com.SkrinVex.syncwave.app.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
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
import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import com.SkrinVex.syncwave.app.domain.model.Track
import com.SkrinVex.syncwave.app.domain.repository.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var mediaSession: MediaSession? = null
    private var progressJob: Job? = null
    private var simpleCache: SimpleCache? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        setupCacheAndPlayer()
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

        val player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressUpdates()
                    startMediaService()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playerState.update { it.copy(isBuffering = true) }
                    }
                    Player.STATE_READY -> {
                        _playerState.update {
                            it.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L),
                                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
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
            mediaSession = MediaSession.Builder(context, player)
                .setId("SyncWaveAudioSession")
                .build()
        } catch (_: Exception) {}
    }

    private fun startMediaService() {
        try {
            val intent = Intent(context, SyncWaveMediaService::class.java)
            ContextCompat.startForegroundService(context, intent)
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

    fun playTrack(track: Track) {
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

        _playerState.update {
            it.copy(
                currentTrack = track,
                currentPositionMs = 0L,
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
    }

    fun skipToQueueItem(index: Int) {
        val queue = _playerState.value.queue
        if (index in queue.indices) {
            _playerState.update { it.copy(currentIndex = index) }
            playTrack(queue[index])
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
        val player = exoPlayer ?: return

        if (player.currentPosition > 3000L) {
            player.seekTo(0)
            return
        }

        val queue = state.queue
        if (queue.isEmpty()) return

        var prevIndex = state.currentIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (state.repeatMode == RepeatMode.ALL) queue.size - 1 else 0
        }

        _playerState.update { it.copy(currentIndex = prevIndex) }
        playTrack(queue[prevIndex])
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
