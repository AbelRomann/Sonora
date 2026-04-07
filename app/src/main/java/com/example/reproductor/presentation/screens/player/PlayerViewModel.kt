package com.example.reproductor.presentation.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.reproductor.domain.model.PlaybackProgress
import com.example.reproductor.domain.model.PlayerState
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.repository.MusicRepository
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.reproductor.presentation.player.EqPreset

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: MusicPlayerController,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = controller.playerState
    val playbackProgress: StateFlow<PlaybackProgress> = controller.playbackProgress
    val repeatMode: StateFlow<Int> = controller.repeatMode
    val shuffleModeEnabled: StateFlow<Boolean> = controller.shuffleModeEnabled
    val sleepTimerRemainingMs: StateFlow<Long?> = controller.sleepTimerRemainingMs
    val eqPreset: StateFlow<EqPreset> = controller.eqPreset

    val playlists: StateFlow<List<Playlist>> = musicRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun togglePlayPause() {
        if (controller.playerState.value.isPlaying) controller.pause()
        else controller.play()
    }

    fun skipToNext() = controller.skipToNext()
    fun skipToPrevious() = controller.skipToPrevious()
    fun seekTo(position: Long) = controller.seekTo(position)
    fun toggleRepeatMode() = controller.toggleRepeatMode()
    fun toggleShuffleMode() = controller.toggleShuffleMode()

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun playNext(song: Song) = controller.playNext(song)
    fun addToQueue(song: Song) = controller.addToQueue(song)

    fun removeFromQueue(index: Int) = controller.removeFromQueue(index)
    fun skipToIndex(index: Int) = controller.skipToIndex(index)
    fun moveQueueItem(from: Int, to: Int) = controller.moveQueueItem(from, to)
    fun clearQueue() = controller.clearQueue()

    // ── Sleep timer ──────────────────────────────────────────────────────────
    fun startSleepTimer(minutes: Int) = controller.startSleepTimer(minutes)
    fun cancelSleepTimer() = controller.cancelSleepTimer()

    // ── Equalizer ────────────────────────────────────────────────────────────
    fun setEqPreset(preset: EqPreset) = controller.setEqPreset(preset)
}

