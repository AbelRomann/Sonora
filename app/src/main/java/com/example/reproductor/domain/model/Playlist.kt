package com.example.reproductor.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long,
    val songs: List<Song> = emptyList()
)