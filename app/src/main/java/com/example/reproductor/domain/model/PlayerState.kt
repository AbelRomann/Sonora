package com.example.reproductor.domain.model

import androidx.compose.runtime.Stable

@Stable
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackMode: PlaybackMode = PlaybackMode.NORMAL,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0
)

enum class PlaybackMode {
    NORMAL,      // Reproducción normal
    REPEAT_ONE,  // Repetir canción actual
    REPEAT_ALL,  // Repetir todas
    SHUFFLE      // Aleatorio
}
