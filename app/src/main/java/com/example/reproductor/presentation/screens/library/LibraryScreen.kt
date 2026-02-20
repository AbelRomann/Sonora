package com.example.reproductor.presentation.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reproductor.domain.model.Playlist
import com.example.reproductor.domain.model.Song
import com.example.reproductor.presentation.components.SongItem
import com.example.reproductor.presentation.library.LibraryViewModel

private enum class LibraryTab { SONGS, PLAYLISTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit,
    onBackClick: () -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val lightListModeEnabled by viewModel.lightListModeEnabled.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedSongIds = remember { mutableStateListOf<Long>() }

    fun toggleSongSelection(songId: Long) {
        if (selectedSongIds.contains(songId)) {
            selectedSongIds.remove(songId)
        } else {
            selectedSongIds.add(songId)
        }

        if (selectedSongIds.isEmpty()) {
            selectionMode = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionMode) "${selectedSongIds.size} seleccionadas" else "Biblioteca"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectionMode) {
                                selectedSongIds.clear()
                                selectionMode = false
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (selectedTab == LibraryTab.SONGS) {
                        IconButton(onClick = { viewModel.toggleLightListMode() }) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = if (lightListModeEnabled) "Desactivar modo lista ligera" else "Activar modo lista ligera",
                                tint = if (lightListModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (selectedTab == LibraryTab.SONGS && !selectionMode && songs.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.playSongs(songs, 0)
                                onNavigateToPlayer()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play all"
                            )
                        }
                    }

                    if (selectedTab == LibraryTab.SONGS && selectionMode) {
                        IconButton(
                            onClick = { showAddToPlaylistDialog = true },
                            enabled = selectedSongIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar a playlist"
                            )
                        }
                    }

                    if (selectedTab == LibraryTab.PLAYLISTS) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Crear playlist")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == LibraryTab.SONGS,
                    onClick = {
                        selectedTab = LibraryTab.SONGS
                    },
                    text = { Text("Canciones") }
                )
                Tab(
                    selected = selectedTab == LibraryTab.PLAYLISTS,
                    onClick = {
                        selectedTab = LibraryTab.PLAYLISTS
                        selectedSongIds.clear()
                        selectionMode = false
                    },
                    text = { Text("Playlists") }
                )
            }

            if (selectedTab == LibraryTab.SONGS) {
                SongsTab(
                    songs = songs,
                    selectedSongIds = selectedSongIds.toSet(),
                    selectionMode = selectionMode,
                    onSongClick = remember(songs, selectionMode, viewModel, onNavigateToPlayer) {
                        { song: Song ->
                            if (selectionMode) {
                                toggleSongSelection(song.id)
                            } else {
                                val index = songs.indexOf(song)
                                viewModel.playSongs(songs, index)
                                onNavigateToPlayer()
                            }
                        }
                    },
                    onSongLongClick = remember(viewModel) {
                        { song: Song ->
                            if (!selectionMode) {
                                selectionMode = true
                            }
                            toggleSongSelection(song.id)
                        }
                    },
                    lightListModeEnabled = lightListModeEnabled
                )
            } else {
                PlaylistsTab(
                    playlists = playlists,
                    onDeletePlaylist = viewModel::deletePlaylist,
                    onOpenPlaylist = onOpenPlaylist
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onConfirm = { playlistId ->
                val songsToAdd = selectedSongIds.toList()
                viewModel.addSongsToPlaylist(playlistId, songsToAdd)
                selectedSongIds.clear()
                selectionMode = false
                showAddToPlaylistDialog = false
            },
            onCreateAndConfirm = { name ->
                val songsToAdd = selectedSongIds.toList()
                viewModel.createPlaylistAndAddSongs(name, songsToAdd)
                selectedSongIds.clear()
                selectionMode = false
                showAddToPlaylistDialog = false
            }
        )
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    selectedSongIds: Set<Long>,
    selectionMode: Boolean,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    lightListModeEnabled: Boolean
) {
    // Índice O(1) para encontrar la posición de la canción al hacer click
    val songIndexMap = remember(songs) { songs.withIndex().associate { (i, s) -> s.id to i } }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${songs.size} canciones",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mantén presionado para seleccionar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(
            items = songs,
            key = { it.id },
            contentType = { "song" }
        ) { song ->
            SongItem(
                song = song,
                onClick = { onSongClick(song) },
                onLongClick = { onSongLongClick(song) },
                isSelected = selectedSongIds.contains(song.id),
                showAlbumArt = !lightListModeEnabled
            )
        }

        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay canciones en tu biblioteca",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onCreateAndConfirm: (String) -> Unit
) {
    var selectedPlaylistId by remember(playlists) { mutableStateOf(playlists.firstOrNull()?.id) }
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar a playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (playlists.isEmpty()) {
                    Text("No tienes playlists. Crea una para continuar.")
                } else {
                    playlists.forEach { playlist ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPlaylistId == playlist.id,
                                onClick = { selectedPlaylistId = playlist.id }
                            )
                            Text("${playlist.name} (${playlist.songCount})")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("O crea una nueva")
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Nombre de playlist") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        onCreateAndConfirm(newPlaylistName)
                    } else {
                        selectedPlaylistId?.let(onConfirm)
                    }
                },
                enabled = newPlaylistName.isNotBlank() || selectedPlaylistId != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    onDeletePlaylist: (Long) -> Unit,
    onOpenPlaylist: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes playlists todavía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = playlists,
                key = { it.id },
                contentType = { "playlist" }
            ) { playlist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { onOpenPlaylist(playlist.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = playlist.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${playlist.songCount} canciones",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar playlist")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva playlist") },
        text = {
            TextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                placeholder = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(playlistName) },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
