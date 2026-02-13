package com.example.reproductor.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val year: Int?,
    val albumArt: String?
)