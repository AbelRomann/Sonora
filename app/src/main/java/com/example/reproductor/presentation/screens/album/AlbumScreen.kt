package com.example.reproductor.presentation.screens.album

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.reproductor.presentation.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    onNavigateToPlayer: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val album by viewModel.album.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album?.name ?: "Álbum") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (songs.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.playAll()
                                onNavigateToPlayer()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play all"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header del álbum
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Portada grande
                    Surface(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(MaterialTheme.shapes.medium),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        if (album?.albumArt != null) {
                            AsyncImage(
                                model = album?.albumArt,
                                contentDescription = "Album art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(60.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nombre del álbum
                    Text(
                        text = album?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Artista
                    Text(
                        text = album?.artist ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Info adicional
                    Text(
                        text = "${songs.size} canciones" +
                                if (album?.year != null) " • ${album?.year}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Canciones
            itemsIndexed(songs, key = { _, s -> s.id }, contentType = { _, _ -> "SongItem" }) { index, song ->
                SongItem(
                    song = song,
                    onClick = {
                        viewModel.playSongs(index)
                        onNavigateToPlayer()
                    }
                )
            }
        }
    }
}