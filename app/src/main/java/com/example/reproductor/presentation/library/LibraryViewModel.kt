package com.example.reproductor.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.repository.MusicRepository
import com.example.reproductor.domain.usecase.GetAlbumsUseCase
import com.example.reproductor.domain.usecase.GetAllSongsUseCase
import com.example.reproductor.domain.usecase.RefreshMusicUseCase
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val refreshMusicUseCase: RefreshMusicUseCase,
    private val musicRepository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = musicRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMusic()
    }

    fun getSongsForPlaylist(playlistId: Long) = musicRepository.getSongsInPlaylist(playlistId)

    private fun loadMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                refreshMusicUseCase()

                launch {
                    getAllSongsUseCase()
                        .catch { e ->
                            _error.value = "Error al cargar canciones: ${e.message}"
                        }
                        .collect { songList ->
                            _songs.value = songList
                        }
                }

                launch {
                    getAlbumsUseCase()
                        .catch { e ->
                            _error.value = "Error al cargar álbumes: ${e.message}"
                        }
                        .collect { albumList ->
                            _albums.value = albumList
                            _isLoading.value = false
                        }
                }
            } catch (e: Exception) {
                _error.value = "Error al escanear música: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            musicRepository.createPlaylist(name.trim())
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun moveSongInPlaylist(playlistId: Long, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            musicRepository.moveSongInPlaylist(playlistId, fromIndex, toIndex)
        }
    }

    fun playSong(song: Song) {
        playerController.playSong(song)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        playerController.playSongs(songs, startIndex)
    }

    fun refreshMusic() {
        loadMusic()
    }

    fun clearError() {
        _error.value = null
    }
}
