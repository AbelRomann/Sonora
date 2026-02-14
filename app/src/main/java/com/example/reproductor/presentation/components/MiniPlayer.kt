package com.example.reproductor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.reproductor.presentation.player.PlayerViewModel

@Composable
fun MiniPlayer(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val currentSong = playerState.currentSong

    if (currentSong != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(onClick = onExpand),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 3.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de progreso
                LinearProgressIndicator(
                    progress = if (playerState.duration > 0) {
                        playerState.currentPosition.toFloat() / playerState.duration.toFloat()
                    } else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Portada
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        if (currentSong.albumArt != null) {
                            AsyncImage(
                                model = currentSong.albumArt,
                                contentDescription = "Album art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Info de la canción
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Controles
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}