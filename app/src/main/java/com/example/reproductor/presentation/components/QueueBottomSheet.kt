package com.example.reproductor.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.reproductor.domain.model.Song
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.roundToInt

// ─── Palette ────────────────────────────────────────────────────
private val QSheetBg    = Color(0xFF0E0E1A)
private val QCardBg     = Color(0xFF181825)
private val QCardActive = Color(0xFF1E1E2E)
private val QAccent     = Color(0xFFE8FF47)
private val QMuted      = Color(0xFF6B6B85)
private val QSubtle     = Color(0xFFB0B0C0)
private val QDivider    = Color(0xFF252535)
private val QArtGrad1   = Color(0xFF7B61FF)
private val QArtGrad2   = Color(0xFFFF5F7E)
private val QArtGrad3   = Color(0xFFFF9A3C)
private val QSwipeBg    = Color(0xFF3A1A1A)
private val QDragBg     = Color(0xFF252540)

// ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSkipToIndex: (Int) -> Unit,
    onRemoveAt: (Int) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit,
    onClearQueue: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = QSheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(QMuted.copy(alpha = 0.5f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            QueueHeader(
                count = queue.size,
                currentSong = queue.getOrNull(currentIndex),
                onClearQueue = onClearQueue
            )

            HorizontalDivider(color = QDivider, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))

            if (queue.isEmpty()) {
                EmptyQueueState()
            } else {
                DragDropQueueList(
                    queue = queue,
                    currentIndex = currentIndex,
                    onSkipToIndex = onSkipToIndex,
                    onRemoveAt = onRemoveAt,
                    onMoveItem = onMoveItem
                )
            }
        }
    }
}

// ── Drag & Drop queue list ────────────────────────────────────────

private data class QueueItem(val key: Long, val song: Song)

@Composable
private fun DragDropQueueList(
    queue: List<Song>,
    currentIndex: Int,
    onSkipToIndex: (Int) -> Unit,
    onRemoveAt: (Int) -> Unit,
    onMoveItem: (from: Int, to: Int) -> Unit
) {
    // Local list with stable unique keys
    var localQueue by remember { mutableStateOf(emptyList<QueueItem>()) }

    LaunchedEffect(queue) {
        val currentSongs = localQueue.map { it.song }
        // Update local items if the actual queue contents (not just drag positions) changed
        if (currentSongs != queue) {
            var counter = 0L
            localQueue = queue.map { QueueItem(counter++, it) }
        }
    }

    val lazyListState = rememberLazyListState()

    // The "now_playing_label" header is a non-reorderable item inserted at LazyColumn
    // index 0 when the label is visible. The reorderable library gives us LazyColumn
    // indices in onMove, but localQueue is 0-based (songs only). We must subtract the
    // header count before touching the list to avoid IndexOutOfBoundsException.
    val headerOffset = if (currentIndex >= 0 && currentIndex < localQueue.size) 1 else 0

    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromIdx = (from.index - headerOffset).coerceIn(localQueue.indices)
            val toIdx   = (to.index   - headerOffset).coerceIn(0, (localQueue.size - 1).coerceAtLeast(0))
            if (fromIdx != toIdx && fromIdx in localQueue.indices) {
                localQueue = localQueue.toMutableList().also { list ->
                    list.add(toIdx, list.removeAt(fromIdx))
                }
            }
        }
    )

    var draggingItemKey by remember { mutableStateOf<Long?>(null) }
    var draggingStartIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ── "Now playing" label ──────────────────────────────────
        if (currentIndex >= 0 && currentIndex < localQueue.size) {
            item(key = "now_playing_label") {
                Text(
                    text = "REPRODUCIENDO",
                    color = QAccent.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }

        itemsIndexed(
            items = localQueue,
            key = { _, item -> item.key }
        ) { index, item ->
            val isActive = index == currentIndex
            val isDraggingThis = draggingItemKey == item.key
            val isAnyDragging = draggingItemKey != null

            ReorderableItem(reorderState, key = item.key) { isDragging ->
                // Separator label before "Up next"
                if (!isActive && index == currentIndex + 1 && !isAnyDragging) {
                    Text(
                        text = "A CONTINUACIÓN",
                        color = QMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp, top = 6.dp)
                    )
                }

                DraggableQueueCard(
                    song = item.song,
                    isActive = isActive,
                    isDragging = isDragging,
                    isDimmed = isAnyDragging && !isDraggingThis,
                    dragHandleModifier = Modifier.draggableHandle(
                        onDragStarted = {
                            draggingItemKey = item.key
                            draggingStartIndex = localQueue.indexOf(item)
                        },
                        onDragStopped = {
                            val oldIndex = draggingStartIndex
                            val newIndex = localQueue.indexOf(item)
                            if (oldIndex != null && newIndex != -1 && oldIndex != newIndex) {
                                onMoveItem(oldIndex, newIndex)
                            }
                            draggingItemKey = null
                            draggingStartIndex = null
                        }
                    ),
                    onTap = { if (!isDragging) onSkipToIndex(index) },
                    onRemove = { onRemoveAt(index) }
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Individual draggable card ─────────────────────────────────────

@Composable
private fun DraggableQueueCard(
    song: Song,
    isActive: Boolean,
    isDragging: Boolean,
    isDimmed: Boolean,
    dragHandleModifier: Modifier,
    onTap: () -> Unit,
    onRemove: () -> Unit
) {
    // Swipe-to-dismiss state
    var offsetX by remember { mutableFloatStateOf(0f) }
    val DISMISS_THRESHOLD = -250f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = if (offsetX == 0f) 300 else 0),
        label = "swipe_offset"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = when {
            isDimmed -> 0.45f
            else -> 1f
        },
        animationSpec = tween(180),
        label = "card_alpha"
    )

    val elevation by animateDpAsState(
        targetValue = when {
            isDragging -> 20.dp
            isActive -> 6.dp
            else -> 0.dp
        },
        animationSpec = tween(200),
        label = "card_elevation"
    )

    val cardBg = when {
        isDragging -> QDragBg
        isActive   -> QCardActive
        else       -> QCardBg
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .alpha(cardAlpha)
    ) {
        // Swipe-to-dismiss red background (only when not dragging)
        if (!isDragging) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(QSwipeBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "✕",
                    color = Color(0xFFFF5F7E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }

        // Card foreground
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = when {
                        isDragging -> QAccent.copy(alpha = 0.35f)
                        isActive   -> QAccent.copy(alpha = 0.2f)
                        else       -> Color.Transparent
                    }
                )
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .then(
                    if (isActive || isDragging) Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                (if (isDragging) QAccent else QAccent).copy(alpha = if (isDragging) 0.14f else 0.08f),
                                Color.Transparent
                            ),
                            endX = 320f
                        )
                    ) else Modifier
                )
                .then(
                    // Dashed-border effect for dragging state via a subtle outer border
                    if (isDragging) Modifier.background(
                        Color.Transparent,
                        RoundedCornerShape(14.dp)
                    ) else Modifier
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < DISMISS_THRESHOLD) onRemove()
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f }
                    ) { _, dragAmount ->
                        if (!isDragging && offsetX + dragAmount < 0)
                            offsetX = (offsetX + dragAmount).coerceAtLeast(DISMISS_THRESHOLD - 80f)
                    }
                }
                .then(dragHandleModifier)
                .clickable(enabled = !isDragging) { onTap() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active / dragging indicator bar
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDragging -> QAccent
                            isActive   -> QAccent
                            else       -> Color.Transparent
                        }
                    )
            )
            Spacer(Modifier.width(10.dp))

            MiniArtwork(albumArt = song.albumArt, size = 46)
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = when {
                        isDragging -> QAccent.copy(alpha = 0.95f)
                        isActive   -> Color.White
                        else       -> QSubtle
                    },
                    fontWeight = if (isActive || isDragging) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    color = QMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = formatDuration(song.duration),
                color = QMuted,
                fontSize = 11.sp
            )

            Spacer(Modifier.width(8.dp))

            // Drag handle — highlighted when dragging
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Reordenar",
                tint = if (isDragging) QAccent.copy(alpha = 0.85f) else QMuted.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Header ───────────────────────────────────────────────────────

@Composable
private fun QueueHeader(
    count: Int,
    currentSong: Song?,
    onClearQueue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "COLA DE REPRODUCCIÓN",
                    color = QAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (count == 0) "Sin canciones"
                           else if (count == 1) "1 canción" else "$count canciones",
                    color = QMuted,
                    fontSize = 12.sp
                )
            }
            if (count > 1) {
                TextButton(onClick = onClearQueue) {
                    Text(
                        text = "Limpiar",
                        color = QMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (currentSong != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(QAccent.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(QAccent)
                )
                Spacer(Modifier.width(10.dp))
                MiniArtwork(albumArt = currentSong.albumArt, size = 40)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        color = QMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(text = "♫", color = QAccent, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Mini artwork ─────────────────────────────────────────────────

@Composable
private fun MiniArtwork(albumArt: String?, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 5).dp))
            .background(Brush.linearGradient(listOf(QArtGrad1, QArtGrad2, QArtGrad3))),
        contentAlignment = Alignment.Center
    ) {
        if (albumArt != null) {
            AsyncImage(
                model = albumArt,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size((size * 0.45f).dp)
            )
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────

@Composable
private fun EmptyQueueState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(QCardBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                tint = QMuted,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "La cola está vacía", color = QSubtle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(text = "Agrega canciones para reproducirlas aquí", color = QMuted, fontSize = 13.sp)
    }
}
