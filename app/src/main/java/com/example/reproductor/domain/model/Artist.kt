package com.example.reproductor.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int
)