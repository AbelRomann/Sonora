package com.example.reproductor.presentation.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.reproductor.domain.model.PlayerState
import com.example.reproductor.domain.model.PlaybackMode
import com.example.reproductor.domain.model.Song
import com.example.reproductor.service.MusicPlayerService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var progressUpdateJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.NORMAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlayerService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                setupPlayerListener()
                startProgressUpdates()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlayerState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState()
            }
        })
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = coroutineScope.launch {
            while (isActive) {
                updateProgress()
                delay(1000) // Actualizar cada segundo
            }
        }
    }

    private fun updateProgress() {
        mediaController?.let { controller ->
            val currentPosition = controller.currentPosition.coerceAtLeast(0)
            val duration = controller.duration.coerceAtLeast(0)

            _playerState.value = _playerState.value.copy(
                currentPosition = currentPosition,
                duration = duration
            )
        }
    }

    private fun updatePlayerState() {
        mediaController?.let { controller ->
            val currentMediaItem = controller.currentMediaItem
            val currentSong = currentMediaItem?.let { item ->
                Song(
                    id = item.mediaId.toLongOrNull() ?: 0,
                    title = item.mediaMetadata.title?.toString() ?: "Unknown",
                    artist = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                    album = item.mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
                    duration = controller.duration.coerceAtLeast(0),
                    path = item.requestMetadata.mediaUri?.toString() ?: "",
                    albumId = 0,
                    artistId = 0,
                    albumArt = item.mediaMetadata.artworkUri?.toString()
                )
            }

            _playerState.value = _playerState.value.copy(
                currentSong = currentSong,
                isPlaying = controller.isPlaying,
                currentPosition = controller.currentPosition.coerceAtLeast(0),
                duration = controller.duration.coerceAtLeast(0)
            )
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        mediaController?.let { controller ->
            val mediaItems = songs.map { song -> song.toMediaItem() }
            controller.setMediaItems(mediaItems, startIndex, 0)
            controller.prepare()
            controller.play()

            _playerState.value = _playerState.value.copy(
                queue = songs,
                currentIndex = startIndex
            )
        }
    }

    fun playSong(song: Song) {
        playSongs(listOf(song), 0)
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNext()
        updateCurrentIndex(1)
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
        updateCurrentIndex(-1)
    }

    private fun updateCurrentIndex(change: Int) {
        val newIndex = (_playerState.value.currentIndex + change)
            .coerceIn(0, _playerState.value.queue.size - 1)
        _playerState.value = _playerState.value.copy(currentIndex = newIndex)
    }

    fun togglePlaybackMode() {
        val newMode = when (_playbackMode.value) {
            PlaybackMode.NORMAL -> PlaybackMode.REPEAT_ALL
            PlaybackMode.REPEAT_ALL -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.NORMAL
        }
        _playbackMode.value = newMode

        mediaController?.let { controller ->
            when (newMode) {
                PlaybackMode.NORMAL -> {
                    controller.repeatMode = Player.REPEAT_MODE_OFF
                    controller.shuffleModeEnabled = false
                }
                PlaybackMode.REPEAT_ALL -> {
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.shuffleModeEnabled = false
                }
                PlaybackMode.REPEAT_ONE -> {
                    controller.repeatMode = Player.REPEAT_MODE_ONE
                    controller.shuffleModeEnabled = false
                }
                PlaybackMode.SHUFFLE -> {
                    controller.repeatMode = Player.REPEAT_MODE_OFF
                    controller.shuffleModeEnabled = true
                }
            }
        }
    }

    fun release() {
        progressUpdateJob?.cancel()
        mediaController?.release()
        mediaController = null
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArt?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(path)
            .setMediaMetadata(metadata)
            .build()
    }
}