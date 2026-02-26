package com.example.reproductor.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.ui.theme.TextMuted

// ── Shared Song Options Bottom Sheet ──────────────────────────────────────────
//
// Used in HomeScreen (recents), LibraryScreen (all songs), and PlaylistDetailScreen.
//
// Parameters:
//   song              – the song being acted on
//   playlists         – all playlists; used for "add to playlist" sub-panel
//   coverGradient     – gradient colors for the song cover thumbnail
//   onDismiss         – called when the sheet should close
//   onPlayNext        – insert song right after current queue position
//   onAddToQueue      – append song at end of queue
//   onAddToPlaylist   – add song to the given playlist by id
//   onRemoveFromPlaylist – non-null only when in a playlist context; shows
//                          the destructive "remove" action
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    playlists: List<Playlist>,
    coverGradient: List<Color>,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null  // null = not in a playlist context
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

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1B2238))
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
                    label = "Añadir a playlist",
                    onClick = { showPlaylistPicker = true }
                )
                SongOptionItem(
                    icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    iconTint = if (song.isFavorite) Color(0xFFFF5F7E) else Color(0xFF6B6B85),
                    iconBg = (if (song.isFavorite) Color(0xFFFF5F7E) else Color(0xFF6B6B85)).copy(alpha = 0.14f),
                    label = if (song.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                    onClick = onToggleFavorite
                )

                // Destructive action — only when inside a playlist
                if (onRemoveFromPlaylist != null) {
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
                }
            } else {
                // ── Playlist picker sub-panel ─────────────────────────────────
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
                        Text(
                            "No hay playlists disponibles",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
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

// ── Reusable option row ────────────────────────────────────────────────────────

@Composable
fun SongOptionItem(
    icon: ImageVector,
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
