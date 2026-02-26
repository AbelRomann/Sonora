package com.example.reproductor.domain.model

import androidx.compose.runtime.Stable

@Stable
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0
)
