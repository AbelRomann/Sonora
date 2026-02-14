package com.example.reproductor.presentation.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reproductor.domain.model.Album
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.repository.MusicRepository
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val albumId: Long = savedStateHandle.get<String>("albumId")?.toLongOrNull() ?: 0L

    private val _album = MutableStateFlow<Album?>(null)
    val album: StateFlow<Album?> = _album.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    init {
        loadAlbumData()
    }

    private fun loadAlbumData() {
        viewModelScope.launch {
            // Obtener canciones del álbum
            repository.getSongsByAlbum(albumId).collect { songList ->
                _songs.value = songList

                // Obtener información del álbum
                if (songList.isNotEmpty()) {
                    val firstSong = songList.first()
                    _album.value = Album(
                        id = albumId,
                        name = firstSong.album,
                        artist = firstSong.artist,
                        artistId = firstSong.artistId,
                        songCount = songList.size,
                        year = null,
                        albumArt = firstSong.albumArt
                    )
                }
            }
        }
    }

    fun playSongs(startIndex: Int) {
        playerController.playSongs(_songs.value, startIndex)
    }

    fun playAll() {
        playerController.playSongs(_songs.value, 0)
    }
}