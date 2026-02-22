package com.example.reproductor.presentation.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.reproductor.domain.model.PlaybackMode
import com.example.reproductor.presentation.components.formatDuration
import com.example.reproductor.presentation.player.PlayerViewModel

// ── Color palette ──────────────────────────────────────────────
private val BgTop = Color(0xFF12022A)
private val BgBottom = Color(0xFF000000)
private val AccentLime = Color(0xFFE8FF47)
private val NeutralMuted = Color(0xFF6B6B85)
private val NeutralDark = Color(0xFF3A3A50)
private val PinkHeart = Color(0xFFFF5F7E)
private val TrackInactive = Color(0xFF1E1E2E)
private val ArtworkGrad1 = Color(0xFF7B61FF)
private val ArtworkGrad2 = Color(0xFFFF5F7E)
private val ArtworkGrad3 = Color(0xFFFF9A3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val playbackMode by viewModel.playbackMode.collectAsStateWithLifecycle()
    val currentSong = playerState.currentSong

    // ── Seek state ──────────────────────────────────────────────
    var isUserSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(playbackProgress.currentPosition, playbackProgress.duration, isUserSeeking) {
        if (!isUserSeeking) {
            sliderPosition = if (playbackProgress.duration > 0) {
                (playbackProgress.currentPosition.toFloat() / playbackProgress.duration.toFloat())
                    .coerceIn(0f, 1f)
            } else 0f
        }
    }

    // ── Root container ──────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(12.dp))

        // ── Top bar ─────────────────────────────────────────────
        TopBar(onBackClick = onBackClick)

        Spacer(Modifier.height(24.dp))

        // ── Album artwork ───────────────────────────────────────
        AlbumArtwork(albumArt = currentSong?.albumArt)

        Spacer(Modifier.height(28.dp))

        // ── Song info ───────────────────────────────────────────
        Text(
            text = currentSong?.title ?: "Sin canción",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = currentSong?.artist ?: "Artista desconocido",
            color = NeutralMuted,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        // Album badge
        if (currentSong?.album != null && currentSong.album.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = currentSong.album.uppercase(),
                color = AccentLime.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AccentLime.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Secondary actions ───────────────────────────────────
        SecondaryActionsRow(
            playbackMode = playbackMode,
            isFavorite = currentSong?.isFavorite == true,
            onTogglePlaybackMode = { viewModel.togglePlaybackMode() }
        )

        Spacer(Modifier.height(16.dp))

        // ── Progress bar ────────────────────────────────────────
        ProgressSection(
            sliderPosition = sliderPosition,
            currentPosition = playbackProgress.currentPosition,
            duration = playbackProgress.duration,
            onValueChange = { isUserSeeking = true; sliderPosition = it },
            onValueChangeFinished = {
                viewModel.seekTo((sliderPosition * playbackProgress.duration).toLong())
                isUserSeeking = false
            }
        )

        Spacer(Modifier.height(8.dp))

        // ── Transport controls ──────────────────────────────────
        TransportControls(
            isPlaying = playerState.isPlaying,
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipToNext() },
            onPrevious = { viewModel.skipToPrevious() }
        )

        Spacer(Modifier.weight(1f))

        // ── Volume bar ──────────────────────────────────────────
        VolumeBar()

        Spacer(Modifier.height(20.dp))
    }
}

// ═══════════════════════════════════════════════════════════════
//  Sub-composables
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = NeutralMuted
            )
        }
        Text(
            text = "REPRODUCIENDO",
            color = NeutralMuted,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { /* menu */ }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Menú",
                tint = NeutralMuted
            )
        }
    }
}

@Composable
private fun AlbumArtwork(albumArt: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(listOf(ArtworkGrad1, ArtworkGrad2, ArtworkGrad3))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (albumArt != null) {
            AsyncImage(
                model = albumArt,
                contentDescription = "Portada del álbum",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun SecondaryActionsRow(
    playbackMode: PlaybackMode,
    isFavorite: Boolean,
    onTogglePlaybackMode: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Repeat / mode icon
        IconButton(onClick = onTogglePlaybackMode, modifier = Modifier.size(40.dp)) {
            val (icon, tint) = when (playbackMode) {
                PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne to AccentLime
                PlaybackMode.REPEAT_ALL -> Icons.Default.Repeat to AccentLime
                PlaybackMode.SHUFFLE -> Icons.Default.Shuffle to AccentLime
                PlaybackMode.NORMAL -> Icons.Default.Replay to NeutralMuted
            }
            Icon(icon, contentDescription = "Modo de reproducción", tint = tint, modifier = Modifier.size(22.dp))
        }

        // Favorite
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorito",
            tint = if (isFavorite) PinkHeart else NeutralMuted,
            modifier = Modifier.size(22.dp)
        )

        // Shuffle indicator
        Icon(
            Icons.Default.Shuffle,
            contentDescription = "Aleatorio",
            tint = if (playbackMode == PlaybackMode.SHUFFLE) AccentLime else NeutralMuted,
            modifier = Modifier.size(22.dp)
        )

        // Queue
        Icon(
            Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = "Cola",
            tint = NeutralMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSection(
    sliderPosition: Float,
    currentPosition: Long,
    duration: Long,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Slider(
        value = sliderPosition,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = Modifier.fillMaxWidth(),
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = AccentLime.copy(alpha = 0.7f),
            inactiveTrackColor = TrackInactive
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatDuration(currentPosition),
            color = NeutralDark,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = formatDuration(duration),
            color = NeutralDark,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replay / rewind
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.Replay,
                contentDescription = "Rebobinar",
                tint = NeutralMuted,
                modifier = Modifier.size(28.dp)
            )
        }

        // Skip previous
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Anterior",
                tint = NeutralMuted,
                modifier = Modifier.size(36.dp)
            )
        }

        // ── PLAY / PAUSE — hero button ──────────────────────────
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = AccentLime.copy(alpha = 0.45f),
                    spotColor = AccentLime.copy(alpha = 0.55f)
                )
                .clip(CircleShape)
                .background(AccentLime)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color(0xFF0A0A0F),
                modifier = Modifier.size(40.dp)
            )
        }

        // Skip next
        IconButton(onClick = onNext) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Siguiente",
                tint = NeutralMuted,
                modifier = Modifier.size(36.dp)
            )
        }

        // Add to queue
        IconButton(onClick = { /* add */ }) {
            Icon(
                Icons.Default.AddBox,
                contentDescription = "Agregar",
                tint = NeutralMuted,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeBar() {
    var volume by remember { mutableFloatStateOf(0.5f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.VolumeDown,
            contentDescription = null,
            tint = NeutralDark,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Slider(
            value = volume,
            onValueChange = { volume = it },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White.copy(alpha = 0.6f),
                activeTrackColor = NeutralMuted.copy(alpha = 0.4f),
                inactiveTrackColor = TrackInactive
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f))
                )
            }
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Default.VolumeUp,
            contentDescription = null,
            tint = NeutralDark,
            modifier = Modifier.size(18.dp)
        )
    }
}
