package com.example.reproductor.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.reproductor.data.local.database.dao.AlbumDao
import com.example.reproductor.data.local.database.dao.PlaylistDao
import com.example.reproductor.data.local.database.dao.SongDao
import com.example.reproductor.data.local.entities.AlbumEntity
import com.example.reproductor.data.local.entities.PlaylistEntity
import com.example.reproductor.data.local.entities.PlaylistSongCrossRef
import com.example.reproductor.data.local.entities.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao
}