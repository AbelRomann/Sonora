package com.example.reproductor.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.reproductor.presentation.player.PlayerViewModel
import com.example.reproductor.ui.theme.AccentLime

@Composable
fun MiniPlayer(onExpand: () -> Unit, modifier: Modifier = Modifier, viewModel: PlayerViewModel = hiltViewModel()) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val currentSong = playerState.currentSong ?: return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onExpand),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = { if (playbackProgress.duration > 0) playbackProgress.currentPosition.toFloat() / playbackProgress.duration.toFloat() else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentSong.albumArt != null) {
                        AsyncImage(
                            model = currentSong.albumArt,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(10.dp))
                    }
                    
                    // Audio Visualizer Overlay
                    if (playerState.isPlaying) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                        PlayingVisualizer(
                            modifier = Modifier.size(24.dp),
                            isPlaying = playerState.isPlaying,
                            color = AccentLime
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentSong.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                    Text(currentSong.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    Icon(if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                }
                IconButton(onClick = { viewModel.skipToNext() }) { Icon(Icons.Default.SkipNext, contentDescription = null) }
            }
        }
    }
}

@Composable
fun PlayingVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse), label = "b3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val h1 by animateFloatAsState(if (isPlaying) bar1 else 0.2f, label = "h1")
        val h2 by animateFloatAsState(if (isPlaying) bar2 else 0.2f, label = "h2")
        val h3 by animateFloatAsState(if (isPlaying) bar3 else 0.2f, label = "h3")

        Box(modifier = Modifier.weight(1f).fillMaxHeight(h1).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h2).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(color))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(h3).clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)).background(color))
    }
}
