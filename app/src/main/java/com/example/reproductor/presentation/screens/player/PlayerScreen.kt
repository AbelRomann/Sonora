@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.reproductor.presentation.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.reproductor.domain.model.PlaybackMode
import com.example.reproductor.presentation.components.formatDuration
import com.example.reproductor.presentation.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(onBackClick: () -> Unit, viewModel: PlayerViewModel = hiltViewModel()) {
    // playerState: solo cambia al cambiar de canción (título, artwork, play/pause)
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    // playbackProgress: se actualiza cada segundo — aislado para no recomponer todo
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackMode by viewModel.playbackMode.collectAsStateWithLifecycle()
    val currentSong = playerState.currentSong

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Now Playing") },
            navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Artwork — solo se recompone cuando cambia currentSong
            if (currentSong?.albumArt != null) {
                AsyncImage(
                    model = currentSong.albumArt,
                    contentDescription = null,
                    modifier = Modifier.size(310.dp).clip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(220.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            // Título y artista — solo se recomponen cuando cambia currentSong
            Text(
                currentSong?.title ?: "Sin canción",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                currentSong?.artist ?: "Artista desconocido",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Slider — se recompone cada segundo pero está aislado del resto de la pantalla
            PlaybackSlider(
                progress = playbackProgress.currentPosition,
                duration = playbackProgress.duration,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.togglePlaybackMode() }) {
                    val icon = when (playbackMode) {
                        PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        PlaybackMode.SHUFFLE -> Icons.Default.Shuffle
                        else -> Icons.Default.Repeat
                    }
                    Icon(icon, contentDescription = null)
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(36.dp))
                }
                FilledIconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(76.dp)) {
                    Icon(
                        if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp)
                    )
                }
                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

/**
 * Composable aislado para el slider de progreso.
 * Al ser un componente separado, solo él se recompone cada vez que avanza la posición,
 * dejando intactos el artwork, el título y los botones de control.
 */
@Composable
private fun PlaybackSlider(
    progress: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    if (!isUserSeeking) {
        sliderPosition = if (duration > 0) {
            (progress.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    Slider(
        value = sliderPosition,
        onValueChange = { isUserSeeking = true; sliderPosition = it },
        onValueChangeFinished = {
            onSeek((sliderPosition * duration).toLong())
            isUserSeeking = false
        },
        modifier = Modifier.fillMaxWidth()
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatDuration(progress), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatDuration(duration), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
