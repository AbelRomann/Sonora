package com.example.reproductor.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.reproductor.data.local.entities.PlaylistEntity
import com.example.reproductor.data.local.entities.PlaylistSongCrossRef
import com.example.reproductor.data.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query(
        """
        SELECT playlists.*, COUNT(playlist_song_cross_ref.songId) AS songCount
        FROM playlists
        LEFT JOIN playlist_song_cross_ref ON playlists.id = playlist_song_cross_ref.playlistId
        GROUP BY playlists.id
        ORDER BY playlists.name ASC
        """
    )
    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(crossRefs: List<PlaylistSongCrossRef>)

    @Delete
    suspend fun deletePlaylistSong(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref ON songs.id = playlist_song_cross_ref.songId
        WHERE playlist_song_cross_ref.playlistId = :playlistId
        ORDER BY playlist_song_cross_ref.position ASC
        """
    )
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getSongIdsByPlaylist(playlistId: Long): List<Long>

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getPlaylistSongCount(playlistId: Long): Int

    @Query(
        "SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND position = :position LIMIT 1"
    )
    suspend fun getSongIdByPosition(playlistId: Long, position: Int): Long?

    @Query("UPDATE playlist_song_cross_ref SET position = :position WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun updateSongPosition(playlistId: Long, songId: Long, position: Int)

    @Query(
        """
        UPDATE playlist_song_cross_ref
        SET position = position - 1
        WHERE playlistId = :playlistId
          AND position > :fromPosition
          AND position <= :toPosition
        """
    )
    suspend fun shiftPositionsDown(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query(
        """
        UPDATE playlist_song_cross_ref
        SET position = position + 1
        WHERE playlistId = :playlistId
          AND position >= :toPosition
          AND position < :fromPosition
        """
    )
    suspend fun shiftPositionsUp(playlistId: Long, fromPosition: Int, toPosition: Int)

    @Query(
        """
        UPDATE playlist_song_cross_ref
        SET position = (
            SELECT COUNT(*)
            FROM playlist_song_cross_ref AS ordered
            WHERE ordered.playlistId = :playlistId
              AND ordered.position < playlist_song_cross_ref.position
        )
        WHERE playlistId = :playlistId
        """
    )
    suspend fun reindexPlaylistPositions(playlistId: Long)

    @Transaction
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        val nextPosition = getMaxPosition(playlistId) + 1
        val crossRefs = songIds.mapIndexed { offset, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                position = nextPosition + offset
            )
        }
        insertPlaylistSongs(crossRefs)
    }

    @Transaction
    suspend fun createPlaylistAndAddSongs(name: String, songIds: List<Long>): Long {
        val playlistId = insertPlaylist(PlaylistEntity(name = name))
        addSongsToPlaylist(playlistId, songIds)
        return playlistId
    }

    @Transaction
    suspend fun moveSongInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val movedSongId = getSongIdByPosition(playlistId, fromIndex) ?: return

        if (fromIndex < toIndex) {
            shiftPositionsDown(playlistId, fromIndex, toIndex)
        } else {
            shiftPositionsUp(playlistId, fromIndex, toIndex)
        }

        updateSongPosition(playlistId, movedSongId, toIndex)
    }

    @Transaction
    suspend fun deleteSongAndReindex(playlistId: Long, songId: Long) {
        deleteSongFromPlaylist(playlistId, songId)
        reindexPlaylistPositions(playlistId)
    }
}
