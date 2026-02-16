package com.example.reproductor.data.local.database.dao

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.reproductor.data.local.entities.PlaylistEntity

data class PlaylistWithSongCount(
    @Embedded val playlist: PlaylistEntity,
    @ColumnInfo(name = "songCount") val songCount: Int
)
