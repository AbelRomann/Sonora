package com.example.reproductor.presentation.screens.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reproductor.presentation.components.showcase.MiniPlayerCard
import com.example.reproductor.presentation.components.showcase.PlaylistCard
import com.example.reproductor.presentation.components.showcase.SongRow

private val tabs = listOf("Inicio", "Biblioteca", "Now Playing", "Playlists", "Artistas", "EQ")

@Composable
fun ShowcaseScreen() {
    var selectedTab by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060609))
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Soundwave Starter Compose",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFE8FF47),
            modifier = Modifier.padding(top = 18.dp, bottom = 4.dp),
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Base de 6 pantallas inspirada en tu diseño HTML/CSS",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB0B0C0),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFE8FF47)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0, 1, 4, 5 -> PlaceholderScreen(tabs[selectedTab])
            2 -> NowPlayingStarter()
            3 -> PlaylistStarter()
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pantalla $title (estructura lista)", color = Color.White, fontWeight = FontWeight.Bold)
        SongRow(
            title = "Blinding Lights",
            artist = "The Weeknd",
            duration = "3:20",
            brush = Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E)))
        )
        SongRow(
            title = "Levitating",
            artist = "Dua Lipa",
            duration = "3:23",
            brush = Brush.linearGradient(listOf(Color(0xFFE8FF47), Color(0xFF7B61FF)))
        )
        MiniPlayerCard(title = "Blinding Lights", artist = "The Weeknd", progress = 0.38f, isPlaying = true)
    }
}

@Composable
private fun NowPlayingStarter() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Now Playing funcional", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Con look&feel de tu propuesta y componentes reutilizables.", color = Color(0xFFB0B0C0), style = MaterialTheme.typography.bodySmall)
        }
        item {
            SongRow(
                title = "Blinding Lights",
                artist = "The Weeknd · After Hours",
                duration = "1:16 / 3:20",
                brush = Brush.linearGradient(listOf(Color(0xFF7B61FF), Color(0xFFFF5F7E)))
            )
        }
        item {
            SongRow(
                title = "Stay With Me",
                artist = "Sam Smith",
                duration = "2:52",
                brush = Brush.linearGradient(listOf(Color(0xFFFF5F7E), Color(0xFFFF9A3C)))
            )
        }
        item {
            MiniPlayerCard(
                title = "Blinding Lights",
                artist = "The Weeknd",
                progress = 0.38f,
                isPlaying = true
            )
        }
    }
}

@Composable
private fun PlaylistStarter() {
    val data = listOf(
        "Mix del Día" to "32 canciones",
        "Chill Vibes" to "18 canciones",
        "Workout" to "24 canciones",
        "Pop 2024" to "55 canciones"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(data) { (name, count) ->
            PlaylistCard(
                name = name,
                songs = count,
                background = Brush.linearGradient(listOf(Color(0xFF1E0A30), Color(0xFF0A1535)))
            )
        }
    }
}
