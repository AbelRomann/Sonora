package com.example.reproductor.presentation.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.reproductor.domain.model.PlaybackProgress
import com.example.reproductor.domain.model.PlayerState
import com.example.reproductor.domain.model.Song
import com.example.reproductor.service.MusicPlayerService
import com.example.reproductor.util.AudioFadeManager
import com.example.reproductor.domain.repository.MusicRepository
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository
) {
    private var mediaController: MediaController? = null
    // Fix #10: SupervisorJob prevents one failure from cancelling the entire scope
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressUpdateJob: Job? = null
    private val audioFadeManager = AudioFadeManager(coroutineScope)

    // ── Play-count threshold (30 s) ──────────────────────────────────────────
    private val minListenMs = 30_000L          // require 30 seconds of listening
    private val minListenMsForHistory = 15_000L // require 15 seconds for recent history
    private var currentListenSongId: Long? = null
    private var listenStartMs: Long? = null    // wall-clock ms when playback started
    private var listenedMs: Long = 0L          // accumulated listening time for current song
    private var playCountCounted: Boolean = false // true once count has been registered
    private var historyCounted: Boolean = false // true once history has been updated
    // Guard to prevent double-commit when both onPositionDiscontinuity and
    // onMediaItemTransition fire for the same automatic loop event.
    private var lastAutoTransitionHandledMs: Long = 0L

    // Estado de canción/reproducción: solo cambia al cambiar de pista, pause/play, modo, etc.
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Progreso de reproducción: se actualiza cada segundo (separado para evitar recomposición completa)
    private val _playbackProgress = MutableStateFlow(PlaybackProgress())
    val playbackProgress: StateFlow<PlaybackProgress> = _playbackProgress.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

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
                mediaController?.let { controller ->
                    _repeatMode.value = controller.repeatMode
                    _shuffleModeEnabled.value = controller.shuffleModeEnabled
                }
                setupPlayerListener()
                // Only start polling if already playing
                if (mediaController?.isPlaying == true) {
                    startProgressUpdates()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerState()
                if (isPlaying) {
                    startProgressUpdates()
                    recordListenStart()   // player resumed — start the clock
                } else {
                    stopProgressUpdates()
                    updateProgress()      // one final update to sync position
                    pauseListenClock()    // player paused — accumulate elapsed time
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Commit any accumulated time for the PREVIOUS song before resetting
                commitListenTimeIfNeeded()

                // Reset counters for the new song
                val newSongId = mediaItem?.mediaId?.toLongOrNull()
                currentListenSongId = newSongId
                listenStartMs = null
                listenedMs = 0L
                playCountCounted = false
                historyCounted = false

                updatePlayerState()

                // Start the listen clock immediately if already playing
                if (mediaController?.isPlaying == true) {
                    recordListenStart()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                // DISCONTINUITY_REASON_AUTO_TRANSITION fires when a song ends and
                // loops (REPEAT_MODE_ONE) or moves to the next item (REPEAT_MODE_ALL).
                // onMediaItemTransition may or may not also fire for the same event,
                // so we use a 500 ms guard to avoid double-committing.
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    val now = System.currentTimeMillis()
                    if (now - lastAutoTransitionHandledMs > 500L) {
                        lastAutoTransitionHandledMs = now
                        commitListenTimeIfNeeded()
                        // Reset counters for the new playback cycle of the same song
                        val newSongId = mediaController?.currentMediaItem?.mediaId?.toLongOrNull()
                        currentListenSongId = newSongId
                        listenStartMs = null
                        listenedMs = 0L
                        playCountCounted = false
                        historyCounted = false
                        if (mediaController?.isPlaying == true) {
                            recordListenStart()
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleModeEnabled.value = shuffleModeEnabled
            }
        })
    }

    private fun startProgressUpdates() {
        if (progressUpdateJob?.isActive == true) return
        progressUpdateJob = coroutineScope.launch {
            while (isActive) {
                updateProgress()
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    // ── Listen-time tracking helpers ─────────────────────────────────────────

    /** Called when playback starts or resumes. Saves the wall-clock start time. */
    private fun recordListenStart() {
        if (listenStartMs == null) {
            listenStartMs = System.currentTimeMillis()
        }
    }

    /** Called when playback pauses. Adds elapsed time to the accumulator. */
    private fun pauseListenClock() {
        listenStartMs?.let { start ->
            listenedMs += System.currentTimeMillis() - start
            listenStartMs = null
        }
        checkListenThresholds()
    }

    /**
     * Commits any in-progress listening time and resets accumulators.
     */
    private fun commitListenTimeIfNeeded() {
        pauseListenClock() // accumulate any running segment
    }

    private fun checkListenThresholds() {
        val songId = currentListenSongId ?: return
        
        // 15 seconds threshold for "Recently Played"
        if (!historyCounted && listenedMs >= minListenMsForHistory) {
            historyCounted = true
            coroutineScope.launch {
                musicRepository.updateLastPlayed(songId, System.currentTimeMillis())
            }
        }
        
        // 30 seconds threshold for "Most Played" (playCount)
        if (!playCountCounted && listenedMs >= minListenMs) {
            playCountCounted = true
            coroutineScope.launch {
                musicRepository.incrementPlayCount(songId)
            }
        }
    }

    // Fix #4: Only emit when values actually change to avoid unnecessary recomposition
    private fun updateProgress() {
        mediaController?.let { controller ->
            val currentPosition = controller.currentPosition.coerceAtLeast(0)
            val duration = controller.duration.coerceAtLeast(0)
            val current = _playbackProgress.value
            if (currentPosition != current.currentPosition || duration != current.duration) {
                _playbackProgress.value = PlaybackProgress(
                    currentPosition = currentPosition,
                    duration = duration
                )
            }
        }
        // Re-evaluate thresholds on the fly while playing so changes reflect immediately
        if (playCountCounted && historyCounted) return
        val currentListenStart = listenStartMs ?: return
        val currentListenedMs = listenedMs + (System.currentTimeMillis() - currentListenStart)
        if (!historyCounted && currentListenedMs >= minListenMsForHistory ||
            !playCountCounted && currentListenedMs >= minListenMs) {
            // Pause/resume clock to update listenedMs and trigger check
            pauseListenClock()
            if (mediaController?.isPlaying == true) {
                recordListenStart()
            }
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
                currentIndex = controller.currentMediaItemIndex
            )

            // Sincronizar progreso inmediatamente al cambiar de pista
            _playbackProgress.value = PlaybackProgress(
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

    fun playNext(song: Song) {
        mediaController?.let { controller ->
            val insertIndex = (controller.currentMediaItemIndex + 1)
                .coerceAtMost(controller.mediaItemCount)
            controller.addMediaItem(insertIndex, song.toMediaItem())
            val updatedQueue = _playerState.value.queue.toMutableList()
            updatedQueue.add(insertIndex, song)
            _playerState.value = _playerState.value.copy(queue = updatedQueue)
        }
    }

    fun addToQueue(song: Song) {
        mediaController?.let { controller ->
            controller.addMediaItem(song.toMediaItem())
            val updatedQueue = _playerState.value.queue.toMutableList()
            updatedQueue.add(song)
            _playerState.value = _playerState.value.copy(queue = updatedQueue)
        }
    }

    fun removeFromQueue(index: Int) {
        mediaController?.let { controller ->
            if (index < 0 || index >= controller.mediaItemCount) return
            controller.removeMediaItem(index)
            val updatedQueue = _playerState.value.queue.toMutableList()
            if (index < updatedQueue.size) updatedQueue.removeAt(index)
            _playerState.value = _playerState.value.copy(queue = updatedQueue)
        }
    }

    fun skipToIndex(index: Int) {
        mediaController?.let { controller ->
            controller.seekTo(index, 0L)
            controller.play()
            _playerState.value = _playerState.value.copy(currentIndex = index)
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        if (from == to) return
        mediaController?.let { controller ->
            controller.moveMediaItem(from, to)
            val updatedQueue = _playerState.value.queue.toMutableList()
            val item = updatedQueue.removeAt(from)
            updatedQueue.add(to, item)
            // Recalculate currentIndex after reorder
            val newCurrentIndex = when {
                _playerState.value.currentIndex == from -> to
                from < _playerState.value.currentIndex && to >= _playerState.value.currentIndex ->
                    _playerState.value.currentIndex - 1
                from > _playerState.value.currentIndex && to <= _playerState.value.currentIndex ->
                    _playerState.value.currentIndex + 1
                else -> _playerState.value.currentIndex
            }
            _playerState.value = _playerState.value.copy(queue = updatedQueue, currentIndex = newCurrentIndex)
        }
    }

    fun clearQueue() {
        mediaController?.let { controller ->
            // Keep only the currently-playing item
            val currentIndex = controller.currentMediaItemIndex
            val count = controller.mediaItemCount
            // Remove items after current
            if (currentIndex + 1 < count) {
                controller.removeMediaItems(currentIndex + 1, count)
            }
            // Remove items before current
            if (currentIndex > 0) {
                controller.removeMediaItems(0, currentIndex)
            }
            val currentSong = _playerState.value.currentSong
            val newQueue = if (currentSong != null) listOf(currentSong) else emptyList()
            _playerState.value = _playerState.value.copy(queue = newQueue, currentIndex = 0)
        }
    }

    fun play() {
        mediaController?.let { audioFadeManager.fadeInAndPlay(it) }
    }

    fun pause() {
        mediaController?.let { audioFadeManager.fadeOutAndPause(it) }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        // Actualizar el progreso inmediatamente para que el Slider no "salte" de vuelta
        _playbackProgress.value = _playbackProgress.value.copy(currentPosition = position)
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun toggleShuffleMode() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    fun release() {
        commitListenTimeIfNeeded() // flush any pending listen time on release
        audioFadeManager.cancelFade()
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