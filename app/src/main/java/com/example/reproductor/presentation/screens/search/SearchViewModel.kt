package com.example.reproductor.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reproductor.domain.model.Song
import com.example.reproductor.domain.usecase.SearchSongsUseCase
import com.example.reproductor.presentation.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val playerController: MusicPlayerController
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Espera 300ms después de que el usuario deje de escribir
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(emptyList())
                    } else {
                        searchSongsUseCase(query)
                    }
                }
                .collect { songs ->
                    _searchResults.value = songs
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun playSong(song: Song) {
        playerController.playSong(song)
    }
}