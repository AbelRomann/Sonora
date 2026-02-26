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
import com.example.reproductor.data.local.entities.SongFtsEntity

@Database(
    entities = [
        SongEntity::class,
        SongFtsEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS `songs_fts` USING FTS4(" +
                    "`title` TEXT NOT NULL, " +
                    "`artist` TEXT NOT NULL, " +
                    "`album` TEXT NOT NULL, " +
                    "content=`songs`)"
                )
                // Usamos content=songs, y para popular la tabla FTS4 inicialmente con content tables, 
                // insertamos todos los rowids de la tabla principal
                database.execSQL(
                    "INSERT INTO songs_fts(songs_fts) VALUES ('rebuild')"
                )
            }
        }
    }
}
