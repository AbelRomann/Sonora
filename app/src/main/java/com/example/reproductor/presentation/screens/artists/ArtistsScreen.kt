package com.example.reproductor.presentation.screens.artists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reproductor.presentation.library.LibraryViewModel

@Composable
fun ArtistsScreen(
    onNavigateToArtistDetail: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val grouped = remember(songs) { songs.groupBy { it.artist }.toList().sortedByDescending { it.second.size } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060609))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.size(12.dp))
            Text("Artistas", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            val top = grouped.firstOrNull()
            if (top != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF1A0A10), Color(0xFF1A1A0A))))
                        .clickable { onNavigateToArtistDetail(top.first) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(33.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFFF5F7E), Color(0xFFFF9A3C)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(top.first.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("MÁS ESCUCHADO", color = Color(0xFFFF5F7E), style = MaterialTheme.typography.labelSmall)
                        Text(top.first, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("${top.second.size} canciones", color = Color(0xFF6B6B85), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        items(grouped.take(20)) { (artist, list) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateToArtistDetail(artist) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFFE8FF47)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(artist.take(2).uppercase(), color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(artist, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("${list.size} canciones", color = Color(0xFF6B6B85), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF3A3A50))
            }
        }

        item { Spacer(Modifier.size(90.dp)) }
    }
}
