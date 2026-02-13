package com.example.reproductor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.reproductor.domain.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumId: Long,
    val artistId: Long,
    val albumArt: String?,
    val dateAdded: Long = System.currentTimeMillis()
)

fun SongEntity.toDomain() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    path = path,
    albumId = albumId,
    artistId = artistId,
    albumArt = albumArt
)