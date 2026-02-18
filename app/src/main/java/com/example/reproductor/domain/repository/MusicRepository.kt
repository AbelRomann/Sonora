package com.example.reproductor.domain.repository

import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getAllAlbums(): Flow<List<Album>>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artistId: Long): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun refreshMusic()
    suspend fun forceRefreshMusic()

    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String): Long
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun createPlaylistAndAddSongs(name: String, songIds: List<Long>): Long
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun moveSongInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int)
}
