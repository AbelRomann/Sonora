package com.example.reproductor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.reproductor.domain.model.Album

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val year: Int?,
    val albumArt: String?
)

fun AlbumEntity.toDomain() = Album(
    id = id,
    name = name,
    artist = artist,
    artistId = artistId,
    songCount = songCount,
    year = year,
    albumArt = albumArt
)