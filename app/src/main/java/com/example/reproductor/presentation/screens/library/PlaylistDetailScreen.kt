package com.example.reproductor.presentation.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.library.LibraryViewModel

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
    val allSongs by viewModel.songs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar canciones")
            }
        }
    ) { paddingValues ->
        if (playlistSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Esta playlist está vacía",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                itemsIndexed(
                    items = playlistSongs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    PlaylistSongRow(
                        song = song,
                        canMoveUp = index > 0,
                        canMoveDown = index < playlistSongs.lastIndex,
                        showDivider = index < playlistSongs.lastIndex,
                        onPlay = {
                            viewModel.playSongs(playlistSongs, index)
                            onNavigateToPlayer()
                        },
                        onRemove = { viewModel.removeSongFromPlaylist(playlistId, song.id) },
                        onMoveUp = { viewModel.moveSongInPlaylist(playlistId, index, index - 1) },
                        onMoveDown = { viewModel.moveSongInPlaylist(playlistId, index, index + 1) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSongsDialog(
            allSongs = allSongs,
            playlistSongs = playlistSongs,
            onDismiss = { showAddDialog = false },
            onConfirm = { selectedSongs ->
                selectedSongs.forEach { song ->
                    viewModel.addSongToPlaylist(playlistId, song.id)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlaylistSongRow(
    song: Song,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    showDivider: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPlay)
                    .padding(vertical = 2.dp)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.width(140.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val enabledTint = MaterialTheme.colorScheme.onSurface
                val disabledTint = MaterialTheme.colorScheme.onSurfaceVariant

                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Subir",
                        tint = if (canMoveUp) enabledTint else disabledTint
                    )
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        contentDescription = "Bajar",
                        tint = if (canMoveDown) enabledTint else disabledTint
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar canción")
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}

@Composable
private fun AddSongsDialog(
    allSongs: List<Song>,
    playlistSongs: List<Song>,
    onDismiss: () -> Unit,
    onConfirm: (List<Song>) -> Unit
) {
    val currentSongIds = remember(playlistSongs) { playlistSongs.map { it.id }.toSet() }
    val selected = remember { mutableStateMapOf<Long, Boolean>() }
    val availableSongs = remember(allSongs, currentSongIds) {
        allSongs.filterNot { currentSongIds.contains(it.id) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar canciones") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(availableSongs) { _, song ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected[song.id] == true,
                            onCheckedChange = { checked -> selected[song.id] = checked }
                        )
                        Column {
                            Text(song.title)
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(availableSongs.filter { selected[it.id] == true })
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
