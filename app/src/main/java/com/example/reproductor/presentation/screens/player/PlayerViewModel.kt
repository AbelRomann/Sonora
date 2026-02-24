package com.example.reproductor.presentation.screens.player

import androidx.lifecycle.ViewModel
import com.example.reproductor.domain.model.PlaybackMode
import com.example.reproductor.domain.model.PlaybackProgress
import com.example.reproductor.domain.model.PlayerState
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: MusicPlayerController
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = controller.playerState
    val playbackProgress: StateFlow<PlaybackProgress> = controller.playbackProgress
    val playbackMode: StateFlow<PlaybackMode> = controller.playbackMode

    fun togglePlayPause() {
        if (controller.playerState.value.isPlaying) controller.pause()
        else controller.play()
    }

    fun skipToNext() = controller.skipToNext()
    fun skipToPrevious() = controller.skipToPrevious()
    fun seekTo(position: Long) = controller.seekTo(position)
    fun togglePlaybackMode() = controller.togglePlaybackMode()

    fun removeFromQueue(index: Int) = controller.removeFromQueue(index)
    fun skipToIndex(index: Int) = controller.skipToIndex(index)
    fun moveQueueItem(from: Int, to: Int) = controller.moveQueueItem(from, to)
    fun clearQueue() = controller.clearQueue()
}
