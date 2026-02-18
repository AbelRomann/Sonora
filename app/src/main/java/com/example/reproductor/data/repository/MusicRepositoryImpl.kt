package com.example.reproductor.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.reproductor.data.local.database.MusicDatabase
import com.example.reproductor.data.local.database.dao.AlbumDao
import com.example.reproductor.data.local.database.dao.PlaylistDao
import com.example.reproductor.data.local.database.dao.SongDao
import com.example.reproductor.data.local.entities.AlbumEntity
import com.example.reproductor.data.local.entities.PlaylistEntity
import com.example.reproductor.data.local.entities.PlaylistSongCrossRef
import com.example.reproductor.data.local.entities.SongEntity
import com.example.reproductor.data.local.entities.toDomain
import com.example.reproductor.data.source.MediaStoreDataSource
import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val playlistDao: PlaylistDao,
    private val mediaStoreDataSource: MediaStoreDataSource
) : MusicRepository {

    private val PREFS_NAME = "music_cache"
    private val KEY_LAST_SCAN = "last_scan_timestamp"
    private val CACHE_VALIDITY_MS = 3600000L

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getAllAlbums(): Flow<List<Album>> {
        return albumDao.getAllAlbums()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return songDao.getSongsByAlbum(albumId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getSongsByArtist(artistId: Long): Flow<List<Song>> {
        return songDao.getSongsByArtist(artistId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun refreshMusic() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastScan = prefs.getLong(KEY_LAST_SCAN, 0)
        val now = System.currentTimeMillis()

        val hasSongs = songDao.getAllSongs()
            .map { it.isNotEmpty() }
            .firstOrNull() ?: false

        val shouldScan = (now - lastScan > CACHE_VALIDITY_MS) || !hasSongs

        if (shouldScan) {
            val songs = mediaStoreDataSource.scanMusic()
            val albums = mediaStoreDataSource.scanAlbums()

            database.withTransaction {
                syncSongsIncrementally(songs)
                syncAlbumsIncrementally(albums)
            }

            prefs.edit().putLong(KEY_LAST_SCAN, now).apply()
        }
    }

    override suspend fun forceRefreshMusic() = withContext(Dispatchers.IO) {
        val songs = mediaStoreDataSource.scanMusic()
        val albums = mediaStoreDataSource.scanAlbums()

        database.withTransaction {
            syncSongsIncrementally(songs)
            syncAlbumsIncrementally(albums)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SCAN, System.currentTimeMillis()).apply()
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { rows ->
            rows.map { row ->
                Playlist(
                    id = row.playlist.id,
                    name = row.playlist.name,
                    songCount = row.songCount,
                    createdAt = row.playlist.createdAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun createPlaylist(name: String): Long {
        return withContext(Dispatchers.IO) {
            playlistDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            val nextPosition = playlistDao.getMaxPosition(playlistId) + 1
            playlistDao.insertPlaylistSong(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = songId,
                    position = nextPosition
                )
            )
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.deleteSongFromPlaylist(playlistId, songId)
            normalizePlaylistPositions(playlistId)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        withContext(Dispatchers.IO) {
            val playlist = playlistDao.getPlaylistById(playlistId)
            playlist?.let {
                playlistDao.deletePlaylist(it)
            }
        }
    }

    override suspend fun moveSongInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        withContext(Dispatchers.IO) {
            val songIds = playlistDao.getSongIdsByPlaylist(playlistId).toMutableList()
            if (fromIndex !in songIds.indices || toIndex !in songIds.indices || fromIndex == toIndex) {
                return@withContext
            }

            val movedSongId = songIds.removeAt(fromIndex)
            songIds.add(toIndex, movedSongId)

            songIds.forEachIndexed { index, songId ->
                playlistDao.updateSongPosition(playlistId, songId, index)
            }
        }
    }

    private suspend fun syncSongsIncrementally(newSongs: List<SongEntity>) {
        val currentSongs = songDao.getSongsSnapshot()
        val currentSongsById = currentSongs.associateBy { it.id }
        val currentSongIds = currentSongsById.keys
        val newSongsById = newSongs.associateBy { it.id }
        val newSongIds = newSongsById.keys

        val removedSongIds = currentSongIds - newSongIds
        val songsToUpsert = newSongs.filter { newSong ->
            val currentSong = currentSongsById[newSong.id]
            currentSong == null || currentSong != newSong
        }

        if (removedSongIds.isNotEmpty()) {
            songDao.deleteSongsByIds(removedSongIds.toList())
        }

        if (songsToUpsert.isNotEmpty()) {
            songDao.insertSongs(songsToUpsert)
        }
    }

    private suspend fun syncAlbumsIncrementally(newAlbums: List<AlbumEntity>) {
        val currentAlbums = albumDao.getAlbumsSnapshot()
        val currentAlbumsById = currentAlbums.associateBy { it.id }
        val currentAlbumIds = currentAlbumsById.keys
        val newAlbumsById = newAlbums.associateBy { it.id }
        val newAlbumIds = newAlbumsById.keys

        val removedAlbumIds = currentAlbumIds - newAlbumIds
        val albumsToUpsert = newAlbums.filter { newAlbum ->
            val currentAlbum = currentAlbumsById[newAlbum.id]
            currentAlbum == null || currentAlbum != newAlbum
        }

        if (removedAlbumIds.isNotEmpty()) {
            albumDao.deleteAlbumsByIds(removedAlbumIds.toList())
        }

        if (albumsToUpsert.isNotEmpty()) {
            albumDao.insertAlbums(albumsToUpsert)
        }
    }

    private suspend fun normalizePlaylistPositions(playlistId: Long) {
        val songIds = playlistDao.getSongIdsByPlaylist(playlistId)
        songIds.forEachIndexed { index, id ->
            playlistDao.updateSongPosition(playlistId, id, index)
        }
    }
}
