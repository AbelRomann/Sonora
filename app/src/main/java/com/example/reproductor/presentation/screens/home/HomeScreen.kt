package com.example.reproductor.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.components.AlbumCard
import com.example.reproductor.presentation.components.SongItem
import com.example.reproductor.presentation.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshMusicOnFirstSessionEntry() }

    val onSongClick = remember(viewModel, onNavigateToPlayer) {
        { song: Song ->
            viewModel.playSong(song)
            onNavigateToPlayer()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tu música", style = MaterialTheme.typography.headlineSmall)
                        Text("Descubre y reproduce al instante", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshMusic() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar biblioteca")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (albums.isNotEmpty()) {
                item {
                    Text(
                        text = "Álbumes destacados",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(albums.take(12), key = { it.id }) { album ->
                            AlbumCard(album = album, onClick = { onNavigateToAlbum(album.id) })
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(18.dp)) }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Para ti", style = MaterialTheme.typography.titleLarge)
                    FilledTonalButton(onClick = onNavigateToLibrary) { Text("Biblioteca") }
                }
            }

            items(songs.take(20), key = { it.id }) { song ->
                SongItem(song = song, onClick = { onSongClick(song) })
            }

            if (songs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No encontramos música aún. Usa actualizar para reescanear tu dispositivo.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
