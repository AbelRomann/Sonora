package com.example.reproductor.presentation.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.components.SongItem
import com.example.reproductor.presentation.components.SongOptionsSheet
import com.example.reproductor.presentation.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToPlayer: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()

    var selectedSong by remember { mutableStateOf<Song?>(null) }

    // Gradient palette cycling (same as HomeScreen)
    val gradientPalette = remember {
        listOf(
            listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E)),
            listOf(Color(0xFF4FD5FF), Color(0xFF1A6AFF)),
            listOf(Color(0xFF00C896), Color(0xFF4FD5FF)),
            listOf(Color(0xFFFFCC4F), Color(0xFFFF6B4A)),
            listOf(Color(0xFFFF73C2), Color(0xFF9C7BFF)),
            listOf(Color(0xFF4FA0FF), Color(0xFF00C896)),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Artista, canción o álbum") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        shape = SearchBarDefaults.inputFieldShape,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { paddingValues ->
        if (searchQuery.isBlank()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Empieza a buscar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                item {
                    Text(
                        text = "${searchResults.size} resultados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                items(searchResults, key = { it.id }, contentType = { "SongItem" }) { song ->
                    SongItem(
                        song = song,
                        onClick = {
                            viewModel.playSong(song)
                            onNavigateToPlayer()
                        },
                        onLongClick = { selectedSong = song },
                        onMoreClick = { selectedSong = song }
                    )
                }
            }
        }
    }

    // ── Song Options Sheet ────────────────────────────────────────────────────
    selectedSong?.let { song ->
        val songIndex = searchResults.indexOf(song).coerceAtLeast(0)
        SongOptionsSheet(
            song = song,
            playlists = playlists,
            coverGradient = gradientPalette[songIndex % gradientPalette.size],
            onDismiss = { selectedSong = null },
            onPlayNext = {
                libraryViewModel.playNext(song)
                selectedSong = null
            },
            onAddToQueue = {
                libraryViewModel.addToQueue(song)
                selectedSong = null
            },
            onAddToPlaylist = { playlistId ->
                libraryViewModel.addSongToPlaylist(playlistId, song.id)
                selectedSong = null
            },
            onToggleFavorite = {
                libraryViewModel.toggleFavorite(song.id)
                selectedSong = null
            }
        )
    }
}
