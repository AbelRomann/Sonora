package com.example.reproductor.domain.model

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
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