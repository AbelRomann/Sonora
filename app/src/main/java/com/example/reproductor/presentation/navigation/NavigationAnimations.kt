package com.example.reproductor.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

// ─────────────────────────────────────────────────────────────────────────────
// Navigation Animation Specs
//
// Each navigation category has its own animation style:
//   1. Top-level   – lightweight horizontal slide (peer sections)
//   2. Hierarchical – deeper slide left/right (drill in/out)
//   3. Player       – vertical slide up/down  (expand/collapse)
//
// Bottom sheets (Queue, SongOptions) use ModalBottomSheet and need no custom
// transition — they already slide up from the bottom automatically.
// ─────────────────────────────────────────────────────────────────────────────

object NavAnimations {

    // ── Durations ────────────────────────────────────────────────────────────
    private const val TOP_LEVEL_DURATION   = 300
    private const val HIERARCHICAL_DURATION = 300
    private const val HIERARCHICAL_POP_DURATION = 250
    private const val PLAYER_DURATION       = 400
    private const val PLAYER_EXIT_DURATION  = 300

    private val defaultEasing = FastOutSlowInEasing

    // ═══════════════════════════════════════════════════════════════════════
    //  1. Top-level navigation  (Home ↔ Library ↔ Playlists ↔ Artists)
    //     Lightweight horizontal slide at 25% width, subtle fade.
    // ═══════════════════════════════════════════════════════════════════════

    /** Screen entering from the right (navigating forward/right in tab order). */
    val topLevelEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * 0.25f).toInt() },
        animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
    ) + fadeIn(
        animationSpec = tween((TOP_LEVEL_DURATION * 0.6f).toInt())
    )

    /** Screen exiting to the left (the old screen slides slightly out). */
    val topLevelExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * 0.15f).toInt() },
        animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
    ) + fadeOut(
        animationSpec = tween((TOP_LEVEL_DURATION * 0.5f).toInt())
    )

    /** Screen re-entering from the left (navigating back/left in tab order). */
    val topLevelPopEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -(fullWidth * 0.25f).toInt() },
        animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
    ) + fadeIn(
        animationSpec = tween((TOP_LEVEL_DURATION * 0.6f).toInt())
    )

    /** Screen exiting to the right when popped. */
    val topLevelPopExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * 0.15f).toInt() },
        animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
    ) + fadeOut(
        animationSpec = tween((TOP_LEVEL_DURATION * 0.5f).toInt())
    )

    // ═══════════════════════════════════════════════════════════════════════
    //  2. Hierarchical navigation  (Library → Playlist → Song list, etc.)
    //     Full-width slide from right. Feels like pushing onto a stack.
    // ═══════════════════════════════════════════════════════════════════════

    /** New detail screen slides in from the right. */
    val hierarchicalEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },  // 100% width
        animationSpec = tween(HIERARCHICAL_DURATION, easing = defaultEasing)
    ) + fadeIn(
        animationSpec = tween((HIERARCHICAL_DURATION * 0.4f).toInt())
    )

    /** Current screen slides slightly left as the detail pushes in. */
    val hierarchicalExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -(it * 0.25f).toInt() },
        animationSpec = tween(HIERARCHICAL_DURATION, easing = defaultEasing)
    ) + fadeOut(
        animationSpec = tween((HIERARCHICAL_DURATION * 0.5f).toInt())
    )

    /** On back: parent re-enters from the left. */
    val hierarchicalPopEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { -(it * 0.25f).toInt() },
        animationSpec = tween(HIERARCHICAL_POP_DURATION, easing = defaultEasing)
    ) + fadeIn(
        animationSpec = tween((HIERARCHICAL_POP_DURATION * 0.5f).toInt())
    )

    /** On back: detail exits to the right. */
    val hierarchicalPopExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },  // 100% width
        animationSpec = tween(HIERARCHICAL_POP_DURATION, easing = defaultEasing)
    ) + fadeOut(
        animationSpec = tween((HIERARCHICAL_POP_DURATION * 0.4f).toInt())
    )

    // ═══════════════════════════════════════════════════════════════════════
    //  3. Player expand / collapse  (MiniPlayer ↔ Full Player)
    //     Vertical slide-up. Smooth but not heavy.
    // ═══════════════════════════════════════════════════════════════════════

    /** Player slides up from the bottom. */
    val playerEnter: EnterTransition = slideInVertically(
        initialOffsetY = { it },  // starts off-screen at the bottom
        animationSpec = tween(PLAYER_DURATION, easing = defaultEasing)
    ) + fadeIn(
        animationSpec = tween((PLAYER_DURATION * 0.3f).toInt())
    )

    /** Existing screen stays in place (no visible exit). */
    val playerExitBehind: ExitTransition = fadeOut(
        animationSpec = tween(1)   // instant, invisible behind the player
    )

    /** Player slides back down when going back. */
    val playerPopExit: ExitTransition = slideOutVertically(
        targetOffsetY = { it },   // slides back to bottom
        animationSpec = tween(PLAYER_EXIT_DURATION, easing = defaultEasing)
    ) + fadeOut(
        animationSpec = tween((PLAYER_EXIT_DURATION * 0.4f).toInt())
    )

    /** Screen behind re-appears when player is dismissed. */
    val playerPopEnterBehind: EnterTransition = fadeIn(
        animationSpec = tween(1)  // instant re-appear
    )
}
