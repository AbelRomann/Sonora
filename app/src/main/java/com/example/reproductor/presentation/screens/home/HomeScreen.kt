package com.example.reproductor.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.library.LibraryViewModel

@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshMusicOnFirstSessionEntry() }

    val onSongClick = remember(viewModel, onNavigateToPlayer) {
        { song: Song ->
            viewModel.playSong(song)
            onNavigateToPlayer()
        }
    }

    val featured = songs.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060609))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Text("BUENOS DÍAS", color = Color(0xFF6B6B85), style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Reproduciendo",
                    color = Color(0xFFF0F0F8),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.refreshMusic() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color(0xFFE8FF47))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (featured != null) onSongClick(featured) },
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(Color(0xFF1E0A30), Color(0xFF0A1535))))
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E))))
                    ) {
                        if (featured?.albumArt != null) {
                            AsyncImage(model = featured?.albumArt, contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("EN REPRODUCCIÓN", color = Color(0xFF7B61FF), style = MaterialTheme.typography.labelSmall)
                    Text(featured?.title ?: "Sin canciones", color = Color.White, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(featured?.artist ?: "Escanea tu biblioteca", color = Color(0xFFB0B0C0), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color(0xFF6B6B85))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(Color(0xFFE8FF47)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        }
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color(0xFF6B6B85))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recientes", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "ver todo →",
                    color = Color(0xFFE8FF47),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onNavigateToLibrary)
                )
            }
        }

        items(songs.take(8), key = { it.id }) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF171722))
                    .clickable { onSongClick(song) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE8FF47), Color(0xFF7B61FF))))
                )
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(song.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Color(0xFF6B6B85), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}
