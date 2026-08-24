package com.SkrinVex.syncwave.app.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(UnstableApi::class)
class AudioPlayerManager(
    private val context: Context,
    private val sessionDataStore: SessionDataStore,
    private val trackRepository: TrackRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) {
                        startProgressUpdates()
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
                                    durationMs = duration.coerceAtLeast(0L),
                                    currentPositionMs = currentPosition.coerceAtLeast(0L)
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
        }
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
        val token = runBlocking { sessionDataStore.tokenFlow.first() } ?: ""
        val streamUrl = trackRepository.getStreamUrl(track.id, token)
        val coverUrl = trackRepository.getCoverUrl(track.id, token)

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(Uri.parse(coverUrl))
            .build()

        val mediaItem = MediaItem.Builder()
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
        _playerState.update { it.copy(isShuffle = !currentShuffle) }
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
        exoPlayer?.release()
        exoPlayer = null
    }
}
