package com.example.reproductor.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reproductor.domain.model.PlaybackProgress
import com.example.reproductor.domain.model.PlayerState
import com.example.reproductor.domain.model.PlaybackMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: MusicPlayerController
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.playerState
    val playbackProgress: StateFlow<PlaybackProgress> = playerController.playbackProgress
    val playbackMode: StateFlow<PlaybackMode> = playerController.playbackMode

    fun togglePlayPause() {
        viewModelScope.launch {
            if (playerState.value.isPlaying) {
                playerController.pause()
            } else {
                playerController.play()
            }
        }
    }

    fun seekTo(position: Long) {
        viewModelScope.launch {
            playerController.seekTo(position)
        }
    }

    fun skipToNext() {
        viewModelScope.launch {
            playerController.skipToNext()
        }
    }

    fun skipToPrevious() {
        viewModelScope.launch {
            playerController.skipToPrevious()
        }
    }

    fun togglePlaybackMode() {
        viewModelScope.launch {
            playerController.togglePlaybackMode()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // NO liberamos el controller aquí porque es Singleton
        // y debe vivir mientras la app esté activa
    }
}