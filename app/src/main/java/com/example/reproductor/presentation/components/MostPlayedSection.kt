package com.example.reproductor.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.reproductor.domain.model.Song
import com.example.reproductor.ui.theme.TextMuted

// ── Stable data class for list items (avoids passing full Song to item composable) ──

@androidx.compose.runtime.Immutable
data class MostPlayedTrack(
    val id: Long,
    val title: String,
    val artworkUri: String?,
    val playCount: Int
)

fun Song.toMostPlayedTrack() = MostPlayedTrack(
    id = id,
    title = title,
    artworkUri = albumArt,
    playCount = playCount
)

// ── Colors (kept local to avoid polluting the global theme) ──

private val MostPlayedCardBg = Color(0xFF1A1A28)
private val MostPlayedCardBorder = Color(0xFF2A2A3E)
private val PlayButtonBg = Color(0xCC000000) // 80% opaque black

// ── Section composable ──

@Composable
fun MostPlayedSection(
    tracks: List<MostPlayedTrack>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null
) {
    if (tracks.isEmpty()) return

    Column(modifier = modifier) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Más reproducido",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (onSeeAllClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver más",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onSeeAllClick)
                )
            }
        }

        // Horizontal carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(end = 20.dp)
        ) {
            items(
                items = tracks,
                key = { it.id },
                contentType = { "MostPlayedItem" }
            ) { track ->
                MostPlayedItem(
                    track = track,
                    onClick = { onItemClick(track.id) }
                )
            }
        }
    }
}

// ── Individual card composable ──

@Composable
private fun MostPlayedItem(
    track: MostPlayedTrack,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Subtle press scale
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "pressScale"
    )

    // Remember heavy objects
    val containerShape = remember { RoundedCornerShape(20.dp) }
    val containerGradient = remember {
        Brush.verticalGradient(
            colors = listOf(MostPlayedCardBg, Color(0xFF141420))
        )
    }
    val artworkOverlayGradient = remember {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x66000000)),
            startY = 80f,
            endY = 260f
        )
    }

    Column(
        modifier = modifier
            .width(148.dp)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = containerShape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(containerShape)
            .background(containerGradient)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular artwork with play overlay
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Artwork
            AsyncImage(
                model = remember(track.artworkUri) {
                    ImageRequest.Builder(context)
                        .data(track.artworkUri)
                        .crossfade(300)
                        .size(240)  // 2x for high-DPI, keeps memory bounded
                        .build()
                },
                contentDescription = track.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MostPlayedCardBorder),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(artworkOverlayGradient)
            )

            // Play button overlay
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(PlayButtonBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Title
        Text(
            text = track.title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        // Play count metadata row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = track.playCount.toString(),
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }
    }
}
