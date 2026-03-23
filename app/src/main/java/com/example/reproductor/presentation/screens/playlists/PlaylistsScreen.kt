package com.example.reproductor.presentation.screens.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.presentation.library.LibraryViewModel
import com.example.reproductor.ui.theme.AccentLime
import com.example.reproductor.ui.theme.PlayerBackground
import com.example.reproductor.ui.theme.TextMuted
import com.example.reproductor.ui.theme.TextSubtle
import kotlinx.coroutines.launch

// No longer importing interaction source — press animation simplified to alpha

// ── Palette for playlist card gradients ────────────────────────────────────────
private val cardGradients = listOf(
    listOf(Color(0xFF4A1A7A), Color(0xFF1A1245)),   // violet → navy
    listOf(Color(0xFF7A1A1A), Color(0xFF3D1A00)),   // ruby → maroon
    listOf(Color(0xFF0D4A3A), Color(0xFF0A2048)),   // teal → ocean
    listOf(Color(0xFF3A2A70), Color(0xFF0D2A5A)),   // indigo → blue
    listOf(Color(0xFF4A3A00), Color(0xFF1A2A0A)),   // amber → forest
    listOf(Color(0xFF5A1A4A), Color(0xFF1A1245)),   // rose → violet
)

private val cardAccents = listOf(
    Color(0xFF9C7BFF),
    Color(0xFFFF7B7B),
    Color(0xFF4FD5A0),
    Color(0xFF4FA0FF),
    Color(0xFFFFCC4F),
    Color(0xFFFF73C2),
)

// Icons used as playlist art when no album art available
private val playlistIcons: List<ImageVector> = listOf(
    Icons.Default.MusicNote,
    Icons.Default.LibraryMusic,
    Icons.Default.Favorite,
    Icons.Default.QueueMusic,
)

// ── Screen ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onNavigateToPlaylistDetail: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    // Long-press state — which playlist was long-pressed
    var longPressedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBackground)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // ── Header (full-width span) ─────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                PlaylistsHeader(
                    playlistCount = playlists.size,
                    onCreateClick = { showCreateDialog = true }
                )
            }

            // ── Empty state (full-width span) ────────────────────────────────
            if (playlists.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyPlaylistsState(onCreateClick = { showCreateDialog = true })
                }
            } else {
                // ── Grid cards — index from itemsIndexed (no indexOf O(n²)) ──
                itemsIndexed(
                    items = playlists,
                    key = { _, p -> p.id }
                ) { index, playlist ->
                    PlaylistGridCard(
                        playlist = playlist,
                        gradient = cardGradients[index % cardGradients.size],
                        accent = cardAccents[index % cardAccents.size],
                        iconVector = playlistIcons[index % playlistIcons.size],
                        onClick = { onNavigateToPlaylistDetail(playlist.id) },
                        onLongClick = { longPressedPlaylist = playlist }
                    )
                }
            }
        }

        // ── FAB ──────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = true,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentLime)
                    .clickable { showCreateDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nueva playlist",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // ── Create-playlist bottom sheet ──────────────────────────────────────────
    if (showCreateDialog) {
        CreatePlaylistSheet(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    // ── Delete-confirmation bottom sheet (triggered by long press) ────────────
    longPressedPlaylist?.let { playlist ->
        ModalBottomSheet(
            onDismissRequest = { longPressedPlaylist = null },
            sheetState = deleteSheetState,
            containerColor = Color(0xFF12121E),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3A5A))
                )
            }
        ) {
            DeletePlaylistSheetContent(
                playlistName = playlist.name,
                songCount = playlist.songCount,
                onCancel = {
                    scope.launch {
                        deleteSheetState.hide()
                    }.invokeOnCompletion { longPressedPlaylist = null }
                },
                onConfirmDelete = {
                    viewModel.deletePlaylist(playlist.id)
                    scope.launch {
                        deleteSheetState.hide()
                    }.invokeOnCompletion { longPressedPlaylist = null }
                }
            )
        }
    }
}

// ── Delete confirmation sheet content ─────────────────────────────────────────

@Composable
fun DeletePlaylistSheetContent(
    playlistName: String,
    songCount: Int,
    onCancel: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color(0xFFFF5F7E),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Eliminar playlist",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "¿Eliminar \"${playlistName}\"?",
            color = Color(0xFFB0B0C8),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (songCount > 0)
                "Se eliminarán $songCount ${if (songCount == 1) "canción" else "canciones"} de esta lista"
            else "Esta playlist está vacía",
            color = Color(0xFF6B6B85),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(28.dp))

        // Confirm delete — destructive
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF3A1A1A))
                .border(1.dp, Color(0xFFFF5F7E).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable(onClick = onConfirmDelete),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5F7E), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Eliminar",
                    color = Color(0xFFFF5F7E),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Cancel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B2238))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cancelar",
                color = Color(0xFF9B9BB0),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun PlaylistsHeader(
    playlistCount: Int,
    onCreateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tu biblioteca",
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Playlists",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                fontWeight = FontWeight.ExtraBold
            )
            if (playlistCount > 0) {
                Text(
                    text = "$playlistCount playlist${if (playlistCount != 1) "s" else ""}",
                    color = TextSubtle,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Subtle + button (complementary to FAB)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B2238))
                .clickable(onClick = onCreateClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Nueva playlist",
                tint = AccentLime,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedPlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1E0A30), Color(0xFF0A1535), Color(0xFF0A2222))
                )
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(18.dp)
    ) {
        // Decorative glow blob
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF7B61FF).copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Cover mosaic / icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 2x2 color mosaic tiles
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFF73C2)))
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF4FD5FF)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFCC4F)))
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF4FD5A0)))
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DESTACADA",
                    color = Color(0xFF9C7BFF),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = playlist.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${playlist.songCount} canciones",
                    color = TextSubtle,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Play button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistGridCard(
    playlist: Playlist,
    gradient: List<Color>,
    accent: Color,
    iconVector: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    // Lightweight press: just alpha, no graphicsLayer scale (scale+shadow = expensive)
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D1120))
            // 1dp accent-tinted border replaces the custom-colored shadow()
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
            .combinedClickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .graphicsLayer { alpha = if (isPressed) 0.75f else 1f }
    ) {
        Column {
            // ── Cover Area ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    // Single linearGradient — radial glow blob removed
                    .background(
                        Brush.linearGradient(
                            colors = listOf(gradient[0], gradient.last()),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(
                                Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Icon on frosted pill background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Color.Black.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.size(34.dp)
                    )
                }
                // verticalGradient text overlay removed — saves one blend pass per card
            }

            // ── Info Area ─────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = playlist.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(accent.copy(alpha = 0.8f), CircleShape)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${playlist.songCount} canciones",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 0.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaylistsState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Decorative icon ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF9C7BFF).copy(alpha = 0.2f), Color.Transparent)
                    )
                )
                .background(Color(0xFF151C2E), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QueueMusic,
                contentDescription = null,
                tint = Color(0xFF9C7BFF),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Aún no tienes playlists",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Crea tu primera playlist y organiza tu música favorita",
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Crear playlist CTA
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(AccentLime)
                .clickable(onClick = onCreateClick)
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Crear playlist",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
