package com.example.reproductor.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.usecase.GetAlbumsUseCase
import com.example.reproductor.domain.usecase.GetAllSongsUseCase
import com.example.reproductor.domain.usecase.RefreshMusicUseCase
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val refreshMusicUseCase: RefreshMusicUseCase,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMusic()
    }

    private fun loadMusic() {
        viewModelScope.launch {
            _isLoading.value = true

            // Escanear música del dispositivo
            try {
                refreshMusicUseCase()
            } catch (e: Exception) {
                // Manejar error
            }

            // Obtener canciones
            getAllSongsUseCase().collect { songList ->
                _songs.value = songList
            }
        }

        viewModelScope.launch {
            // Obtener álbumes
            getAlbumsUseCase().collect { albumList ->
                _albums.value = albumList
                _isLoading.value = false
            }
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
}