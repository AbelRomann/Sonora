package com.example.reproductor.presentation.screens.playlists

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reproductor.ui.theme.AccentLime
import com.example.reproductor.ui.theme.PlayerBackground
import com.example.reproductor.ui.theme.TextMuted
import com.example.reproductor.ui.theme.TextSubtle

// ── Data ───────────────────────────────────────────────────────────────────────

private data class PlaylistIconOption(val emoji: String, val accent: Color)

private val iconOptions = listOf(
    PlaylistIconOption("🎵", Color(0xFF9C7BFF)),
    PlaylistIconOption("🔥", Color(0xFFFF6B4A)),
    PlaylistIconOption("✨", Color(0xFFFFD700)),
    PlaylistIconOption("💜", Color(0xFFB44FFF)),
    PlaylistIconOption("🎸", Color(0xFF4FD5FF)),
    PlaylistIconOption("🌙", Color(0xFF6B99FF)),
    PlaylistIconOption("⚡", Color(0xFFE8FF47)),
)

private val accentColors = listOf(
    Color(0xFF9C7BFF),   // violet
    Color(0xFFE8FF47),   // lime
    Color(0xFFFF6B4A),   // coral
    Color(0xFF4FD5A0),   // mint
    Color(0xFF4FA0FF),   // blue
    Color(0xFFFF73C2),   // pink
    Color(0xFFFFCC4F),   // amber
)

// ── Sheet ──────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboard = LocalSoftwareKeyboardController.current

    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableIntStateOf(0) }
    var selectedAccent by remember { mutableIntStateOf(0) }
    var isPrivate by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }

    // Auto-focus the name field when sheet opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF111827),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D3A55))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Icon preview ────────────────────────────────────────────────
            val previewAccent by animateColorAsState(
                targetValue = accentColors[selectedAccent],
                animationSpec = spring(),
                label = "accent"
            )
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = previewAccent.copy(alpha = 0.5f),
                        spotColor = previewAccent.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF1B2238))
                    .border(2.dp, previewAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconOptions[selectedIcon].emoji,
                    fontSize = 38.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Title ───────────────────────────────────────────────────────
            Text(
                text = "Nueva Playlist",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Dale nombre y personalízala",
                color = TextSubtle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // ── Name field ──────────────────────────────────────────────────
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Ej: Mis Favoritas 2024",
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = previewAccent,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B2238),
                    unfocusedContainerColor = Color(0xFF1B2238),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = previewAccent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = previewAccent,
                    unfocusedLeadingIconColor = TextMuted
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(24.dp))

            // ── Icon picker ─────────────────────────────────────────────────
            SectionLabel("ÍCONO")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                iconOptions.forEachIndexed { index, option ->
                    val isSelected = index == selectedIcon
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) option.accent.copy(alpha = 0.22f) else Color(0xFF1B2238),
                        animationSpec = spring(),
                        label = "icon_bg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatioBox()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .then(
                                if (isSelected) Modifier.border(
                                    1.5.dp,
                                    option.accent,
                                    RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .clickable { selectedIcon = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(option.emoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Color accent picker ─────────────────────────────────────────
            SectionLabel("COLOR")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                accentColors.forEachIndexed { index, color ->
                    val isSelected = index == selectedAccent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier
                                    .border(2.5.dp, Color.White, CircleShape)
                                    .shadow(8.dp, CircleShape, ambientColor = color, spotColor = color)
                                else Modifier
                            )
                            .clickable { selectedAccent = index }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Visibility toggle ───────────────────────────────────────────
            SectionLabel("VISIBILIDAD")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B2238))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VisibilityChip(
                    label = "Privada",
                    icon = Icons.Default.Lock,
                    selected = isPrivate,
                    accent = previewAccent,
                    modifier = Modifier.weight(1f)
                ) { isPrivate = true }

                VisibilityChip(
                    label = "Pública",
                    icon = Icons.Default.Public,
                    selected = !isPrivate,
                    accent = previewAccent,
                    modifier = Modifier.weight(1f)
                ) { isPrivate = false }
            }

            Spacer(Modifier.height(28.dp))

            // ── Create button ───────────────────────────────────────────────
            val canCreate = name.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = if (canCreate) 20.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = AccentLime.copy(alpha = 0.4f),
                        spotColor = AccentLime.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (canCreate) AccentLime else Color(0xFF1B2238))
                    .clickable(enabled = canCreate) {
                        keyboard?.hide()
                        onCreate(name.trim())
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Crear Playlist",
                        color = if (canCreate) Color.Black else TextMuted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (canCreate) {
                        Spacer(Modifier.width(6.dp))
                        Text("→", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

// ── Helper composables ─────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextMuted,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.5.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun Modifier.aspectRatioBox() = this.then(Modifier.height(44.dp))

@Composable
private fun VisibilityChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(),
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) accent else TextMuted,
        animationSpec = spring(),
        label = "chip_content"
    )
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .then(
                if (selected) Modifier.border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
