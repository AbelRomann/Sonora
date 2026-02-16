package com.example.reproductor.data.repository

import android.content.Context
import com.example.reproductor.data.local.database.dao.AlbumDao
import com.example.reproductor.data.local.database.dao.PlaylistDao
import com.example.reproductor.data.local.database.dao.SongDao
import com.example.reproductor.data.local.entities.PlaylistEntity
import com.example.reproductor.data.local.entities.PlaylistSongCrossRef
import com.example.reproductor.data.local.entities.toDomain
import com.example.reproductor.data.source.MediaStoreDataSource
import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class MusicRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val playlistDao: PlaylistDao,
    private val mediaStoreDataSource: MediaStoreDataSource
) : MusicRepository {

    private val PREFS_NAME = "music_cache"
    private val KEY_LAST_SCAN = "last_scan_timestamp"
    private val CACHE_VALIDITY_MS = 3600000L // 1 hora

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
        // Verificar si necesita actualizar basado en caché
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastScan = prefs.getLong(KEY_LAST_SCAN, 0)
        val now = System.currentTimeMillis()

        // Verificar si la base de datos está vacía
        val hasSongs = songDao.getAllSongs()
            .map { it.isNotEmpty() }
            .firstOrNull() ?: false

        // Solo escanear si ha pasado más de 1 hora o si no hay datos
        val shouldScan = (now - lastScan > CACHE_VALIDITY_MS) || !hasSongs

        if (shouldScan) {
            val songs = mediaStoreDataSource.scanMusic()
            val albums = mediaStoreDataSource.scanAlbums()

            songDao.deleteAllSongs()
            albumDao.deleteAllAlbums()

            songDao.insertSongs(songs)
            albumDao.insertAlbums(albums)

            // Actualizar timestamp del último escaneo
            prefs.edit().putLong(KEY_LAST_SCAN, now).apply()
        }
    }

    override suspend fun forceRefreshMusic() = withContext(Dispatchers.IO) {
        val songs = mediaStoreDataSource.scanMusic()
        val albums = mediaStoreDataSource.scanAlbums()

        songDao.deleteAllSongs()
        albumDao.deleteAllAlbums()

        songDao.insertSongs(songs)
        albumDao.insertAlbums(albums)

        // Actualizar timestamp
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SCAN, System.currentTimeMillis()).apply()
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songCount = 0,
                    createdAt = entity.createdAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createPlaylist(name: String): Long {
        return withContext(Dispatchers.IO) {
            playlistDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.insertPlaylistSong(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = songId,
                    position = 0
                )
            )
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        withContext(Dispatchers.IO) {
            playlistDao.deletePlaylistSong(
                PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = songId,
                    position = 0
                )
            )
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
}