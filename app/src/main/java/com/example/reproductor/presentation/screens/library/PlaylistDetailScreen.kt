package com.example.reproductor.presentation.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.components.formatDuration
import com.example.reproductor.presentation.library.LibraryViewModel
import com.example.reproductor.ui.theme.AccentLime
import com.example.reproductor.ui.theme.PlayerBackground
import com.example.reproductor.ui.theme.TextMuted
import com.example.reproductor.ui.theme.TextSubtle

// ── Cover gradients cycling per-song ──────────────────────────────────────────
private val coverBrushes = listOf(
    listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E)),
    listOf(Color(0xFF4FD5FF), Color(0xFF1A6AFF)),
    listOf(Color(0xFF00C896), Color(0xFF4FD5FF)),
    listOf(Color(0xFFFFCC4F), Color(0xFFFF6B4A)),
    listOf(Color(0xFFFF73C2), Color(0xFF9C7BFF)),
    listOf(Color(0xFF4FA0FF), Color(0xFF00C896)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    onNavigateToPlayer: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlistSongsFlow = remember(playlistId) { viewModel.getSongsForPlaylist(playlistId) }
    val playlistSongs by playlistSongsFlow.collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())
    val allSongs by viewModel.songs.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }

    val playlist = remember(playlists, playlistId) { playlists.find { it.id == playlistId } }
    val playlistName = playlist?.name ?: "Mi Playlist"
    val isEmpty = playlistSongs.isEmpty()

    // Total duration
    val totalDurationMs = remember(playlistSongs) { playlistSongs.sumOf { it.duration } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
            .navigationBarsPadding()
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            PlaylistHeader(
                name = playlistName,
                songCount = playlistSongs.size,
                totalDurationMs = totalDurationMs,
                onBackClick = onBackClick,
                onAddClick = { showAddSheet = true },
                canPlay = !isEmpty,
                onPlayClick = {
                    viewModel.playSongs(playlistSongs, 0)
                    onNavigateToPlayer()
                },
                onShuffleClick = {
                    viewModel.playSongs(playlistSongs.shuffled(), 0)
                    onNavigateToPlayer()
                }
            )
        }

        // ── Empty state ──────────────────────────────────────────────────────
        if (isEmpty) {
            item {
                EmptyPlaylistContent(onAddClick = { showAddSheet = true })
            }
        } else {
            // ── Add more row ─────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${playlistSongs.size} canciones · ${formatDuration(totalDurationMs)}",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF1B2238))
                            .clickable { showAddSheet = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AccentLime, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Añadir", color = AccentLime, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── Song rows ────────────────────────────────────────────────────
            itemsIndexed(
                items = playlistSongs,
                key = { _, song -> song.id }
            ) { index, song ->
                PlaylistSongRow(
                    song = song,
                    index = index,
                    gradient = coverBrushes[index % coverBrushes.size],
                    isLast = index == playlistSongs.lastIndex,
                    onPlay = {
                        viewModel.playSongs(playlistSongs, index)
                        onNavigateToPlayer()
                    },
                    onMoreClick = { selectedSongForOptions = song }
                )
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (showAddSheet) {
        AddSongsSheet(
            allSongs = allSongs,
            playlistSongs = playlistSongs,
            playlistName = playlistName,
            onDismiss = { showAddSheet = false },
            onConfirm = { selectedSongs ->
                selectedSongs.forEach { song ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                }
                showAddSheet = false
            }
        )
    }

    selectedSongForOptions?.let { song ->
        SongOptionsSheet(
            song = song,
            playlists = playlists.filter { it.id != playlistId },
            coverGradient = coverBrushes[playlistSongs.indexOf(song).coerceAtLeast(0) % coverBrushes.size],
            onDismiss = { selectedSongForOptions = null },
            onPlayNext = {
                viewModel.playNext(song)
                selectedSongForOptions = null
            },
            onAddToQueue = {
                viewModel.addToQueue(song)
                selectedSongForOptions = null
            },
            onAddToPlaylist = { targetPlaylistId ->
                viewModel.addSongToPlaylist(targetPlaylistId, song.id)
                selectedSongForOptions = null
            },
            onRemoveFromPlaylist = {
                viewModel.removeSongFromPlaylist(playlistId, song.id)
                selectedSongForOptions = null
            }
        )
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistHeader(
    name: String,
    songCount: Int,
    totalDurationMs: Long,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    canPlay: Boolean,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (canPlay) 340.dp else 310.dp)
    ) {
        // Layered gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF2A1050),
                            0.6f to Color(0xFF0F1A35),
                            1.0f to PlayerBackground
                        )
                    )
                )
        )
        // Decorative radial glow
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF7B61FF).copy(alpha = 0.25f), Color.Transparent)),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top nav
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Playlist",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Cover + title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Cover art
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF3A1A6A), Color(0xFF1A1A45))))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (canPlay) {
                        // Mosaic cover
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(coverBrushes[0])))
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(coverBrushes[1])))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(coverBrushes[2])))
                                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(coverBrushes[3])))
                            }
                        }
                    } else {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF9C7BFF).copy(alpha = 0.6f), modifier = Modifier.size(52.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Text(
                        text = name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (canPlay) "$songCount canciones · ${formatDuration(totalDurationMs)}"
                               else "0 canciones · Privada",
                        color = TextSubtle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            if (canPlay) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Play (primary / dominant)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(AccentLime)
                            .clickable(onClick = onPlayClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reproducir", color = Color.Black, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    // Shuffle (secondary)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B2238))
                            .border(1.dp, Color(0xFF2D3A55), CircleShape)
                            .clickable(onClick = onShuffleClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = TextSubtle, modifier = Modifier.size(20.dp))
                    }
                    // Edit (secondary)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B2238))
                            .border(1.dp, Color(0xFF2D3A55), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSubtle, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                // Empty-state primary action
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(AccentLime)
                            .clickable(onClick = onAddClick)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Añadir canciones", color = Color.Black, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF1B2238))
                            .border(1.dp, Color(0xFF2D3A55), RoundedCornerShape(50.dp))
                            .clickable { }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Editar", color = TextSubtle, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Song row ───────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistSongRow(
    song: Song,
    index: Int,
    gradient: List<Color>,
    isLast: Boolean,
    onPlay: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Index number
        Text(
            text = "${index + 1}",
            color = TextMuted.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.End
        )

        // Gradient cover
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Title + artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration
        Text(
            text = formatDuration(song.duration),
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 2.dp)
        )

        // More options icon
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = TextMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 82.dp, end = 20.dp)
                .height(0.5.dp)
                .background(Color(0xFF1B2238))
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyPlaylistContent(onAddClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF13192A))
                .border(1.dp, Color(0xFF1B2238), RoundedCornerShape(24.dp))
                .padding(vertical = 44.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF9C7BFF).copy(alpha = 0.12f), Color.Transparent)),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF1B2238), shape = CircleShape)
                            .border(1.5.dp, Color(0xFF9C7BFF).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF9C7BFF), modifier = Modifier.size(36.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aún no hay canciones", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Agrega canciones desde tu biblioteca para empezar",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF1E2A48))
                        .border(1.5.dp, Color(0xFF9C7BFF).copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                        .clickable(onClick = onAddClick)
                        .padding(horizontal = 28.dp, vertical = 13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = Color(0xFF9C7BFF), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Explorar biblioteca", color = Color(0xFF9C7BFF), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Esta playlist está esperando ser llenada ✨", color = TextMuted.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(100.dp))
    }
}

// ── Song Options Bottom Sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongOptionsSheet(
    song: Song,
    playlists: List<com.example.reproductor.domain.model.Playlist>,
    coverGradient: List<Color>,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onRemoveFromPlaylist: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPlaylistPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D1320),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.18f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // ── Song info header ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(coverGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFF1B2238)
            )

            Spacer(Modifier.height(8.dp))

            // ── Actions ──────────────────────────────────────────────────────
            if (!showPlaylistPicker) {
                SongOptionItem(
                    icon = Icons.Default.SkipNext,
                    iconTint = Color(0xFF7B61FF),
                    iconBg = Color(0xFF7B61FF).copy(alpha = 0.14f),
                    label = "Reproducir a continuación",
                    onClick = onPlayNext
                )
                SongOptionItem(
                    icon = Icons.Default.QueueMusic,
                    iconTint = Color(0xFF4FD5FF),
                    iconBg = Color(0xFF4FD5FF).copy(alpha = 0.14f),
                    label = "Añadir a la cola",
                    onClick = onAddToQueue
                )
                SongOptionItem(
                    icon = Icons.Default.LibraryAdd,
                    iconTint = Color(0xFF00C896),
                    iconBg = Color(0xFF00C896).copy(alpha = 0.14f),
                    label = "Añadir a otra playlist",
                    onClick = { showPlaylistPicker = true }
                )

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color(0xFF1B2238),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))

                SongOptionItem(
                    icon = Icons.Default.Delete,
                    iconTint = Color(0xFFFF5F7E),
                    iconBg = Color(0xFFFF5F7E).copy(alpha = 0.14f),
                    label = "Quitar de la playlist",
                    labelColor = Color(0xFFFF5F7E),
                    onClick = onRemoveFromPlaylist
                )
            } else {
                // ── Playlist picker sub-panel ─────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showPlaylistPicker = false }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        "Elegir playlist",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay otras playlists", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    playlists.forEach { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddToPlaylist(playlist.id) }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1B2238)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = Color(0xFF00C896),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = playlist.name,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color(0xFF1B2238),
                            modifier = Modifier.padding(start = 74.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    labelColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Add Songs Bottom Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongsSheet(
    allSongs: List<Song>,
    playlistSongs: List<Song>,
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: (List<Song>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboard = LocalSoftwareKeyboardController.current

    val currentSongIds = remember(playlistSongs) { playlistSongs.map { it.id }.toSet() }
    val selected = remember { mutableStateMapOf<Long, Boolean>() }
    val availableSongs = remember(allSongs, currentSongIds) { allSongs.filterNot { currentSongIds.contains(it.id) } }

    var query by remember { mutableStateOf("") }
    val filtered by remember(query, availableSongs) {
        derivedStateOf {
            if (query.isBlank()) availableSongs
            else availableSongs.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
            }
        }
    }

    val selectedCount by remember(selected.values.toList()) {
        derivedStateOf { selected.values.count { it } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D1320),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // ── Sheet header ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        text = "Añadir canciones",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (selectedCount > 0) {
                        Text(
                            text = "$selectedCount ${if (selectedCount == 1) "canción seleccionada" else "canciones seleccionadas"}",
                            color = AccentLime,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                // LISTO button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedCount > 0) AccentLime else Color(0xFF1B2238))
                        .clickable(enabled = selectedCount > 0) {
                            keyboard?.hide()
                            onConfirm(availableSongs.filter { selected[it.id] == true })
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "LISTO",
                        color = if (selectedCount > 0) Color.Black else TextMuted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ── Search field ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text("Busca en biblioteca...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A2236),
                        unfocusedContainerColor = Color(0xFF1A2236),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentLime,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // ── Song list ────────────────────────────────────────────────────
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filtered, key = { _, song -> song.id }) { index, song ->
                    val isSelected = selected[song.id] == true
                    val rowBg by animateColorAsState(
                        targetValue = if (isSelected) AccentLime.copy(alpha = 0.06f) else Color.Transparent,
                        animationSpec = spring(),
                        label = "row_bg"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .clickable { selected[song.id] = !isSelected }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Checkbox circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) AccentLime else Color.Transparent
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) AccentLime else Color(0xFF2D3A55),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Gradient cover
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(coverBrushes[index % coverBrushes.size]))
                                .alpha(if (isSelected) 1f else 0.75f),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                        }

                        // Title + artist
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Duration
                        Text(
                            text = formatDuration(song.duration),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Divider
                    if (index < filtered.lastIndex) {
                        Box(Modifier.fillMaxWidth().padding(start = 82.dp).height(0.5.dp).background(Color(0xFF1B2238)))
                    }
                }
            }

            // ── Bottom confirm bar ────────────────────────────────────────────
            if (selectedCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1B2238))
                            .border(1.dp, AccentLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Agregar $selectedCount ${if (selectedCount == 1) "canción" else "canciones"}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "a \"$playlistName\"",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(AccentLime)
                                .clickable {
                                    keyboard?.hide()
                                    onConfirm(availableSongs.filter { selected[it.id] == true })
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Agregar", color = Color.Black, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.width(4.dp))
                                Text("→", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
