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
import androidx.navigation.NavBackStackEntry

// ─────────────────────────────────────────────────────────────────────────────
// Navigation Animation Specs
//
// Three animation categories:
//   1. Top-level   – direction-aware horizontal slide (peer tabs)
//   2. Hierarchical – full-width slide left/right (push into / pop out of detail)
//   3. Player       – vertical slide up/down (expand/collapse)
// ─────────────────────────────────────────────────────────────────────────────

// Tab order defines left-to-right visual position in the bottom nav bar.
internal val TAB_ORDER = listOf(
    Screen.Home.route,
    Screen.Library.route,
    Screen.Playlists.route,
    Screen.Artists.route,
)

/**
 * Returns true if [target] is visually to the RIGHT of [initial] in the tab bar.
 * Forward (right) → new screen enters from the right.
 * Backward (left) → new screen enters from the left.
 */
fun isForwardTabNavigation(initial: NavBackStackEntry?, target: NavBackStackEntry?): Boolean {
    val from = TAB_ORDER.indexOf(initial?.destination?.route)
    val to   = TAB_ORDER.indexOf(target?.destination?.route)
    // If either route is not in the tab list (e.g. a detail screen), default to forward.
    return to >= from
}

object NavAnimations {

    // ── Durations ────────────────────────────────────────────────────────────
    private const val TOP_LEVEL_DURATION        = 180
    private const val HIERARCHICAL_DURATION     = 200
    private const val HIERARCHICAL_POP_DURATION = 180
    private const val PLAYER_DURATION           = 280
    private const val PLAYER_EXIT_DURATION      = 220

    private val defaultEasing = FastOutSlowInEasing

    // ═══════════════════════════════════════════════════════════════════════
    //  1. Top-level navigation  (Home ↔ Library ↔ Playlists ↔ Artists)
    //     Direction-aware: the slide direction mirrors tab position.
    //
    //  Going RIGHT  (e.g. Library → Playlists):
    //    new  → enters from RIGHT (+30%)
    //    old  → exits  to  LEFT  (-30%)
    //
    //  Going LEFT   (e.g. Playlists → Library):
    //    new  → enters from LEFT  (-30%)
    //    old  → exits  to  RIGHT (+30%)
    // ═══════════════════════════════════════════════════════════════════════

    /** Direction-aware enter for a top-level tab screen. */
    fun topLevelEnter(fromEntry: NavBackStackEntry?, toEntry: NavBackStackEntry?): EnterTransition {
        val forward = isForwardTabNavigation(fromEntry, toEntry)
        return slideInHorizontally(
            initialOffsetX = { if (forward) (it * 0.30f).toInt() else -(it * 0.30f).toInt() },
            animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
        ) + fadeIn(animationSpec = tween((TOP_LEVEL_DURATION * 0.65f).toInt()))
    }

    /** Direction-aware exit for a top-level tab screen (peer → peer). */
    fun topLevelExit(fromEntry: NavBackStackEntry?, toEntry: NavBackStackEntry?): ExitTransition {
        val forward = isForwardTabNavigation(fromEntry, toEntry)
        return slideOutHorizontally(
            targetOffsetX = { if (forward) -(it * 0.30f).toInt() else (it * 0.30f).toInt() },
            animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
        ) + fadeOut(animationSpec = tween((TOP_LEVEL_DURATION * 0.50f).toInt()))
    }

    // ── Static fallbacks (used in popExitTransition for top-level screens) ──

    /** Top-level screen exits to the right when popped (via popBackStack). */
    val topLevelPopExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { (it * 0.30f).toInt() },
        animationSpec = tween(TOP_LEVEL_DURATION, easing = defaultEasing)
    ) + fadeOut(animationSpec = tween((TOP_LEVEL_DURATION * 0.50f).toInt()))

    // ═══════════════════════════════════════════════════════════════════════
    //  2. Hierarchical navigation  (any top-level → detail screen)
    //
    //  FORWARD  (push detail):
    //    new  → enters from RIGHT (+100%)    [hierarchicalEnter]
    //    old  → exits  to  LEFT  (-33%)      [hierarchicalExit]
    //
    //  BACK     (pop detail):
    //    old  → exits  to  RIGHT (+100%)     [hierarchicalPopExit]
    //    new  → enters from LEFT (-33%)      [hierarchicalPopEnter]
    // ═══════════════════════════════════════════════════════════════════════

    /** New detail screen slides in from the right. */
    val hierarchicalEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(HIERARCHICAL_DURATION, easing = defaultEasing)
    ) + fadeIn(animationSpec = tween((HIERARCHICAL_DURATION * 0.35f).toInt()))

    /** Parent screen retreats left as detail pushes in. */
    val hierarchicalExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -(it / 3) },
        animationSpec = tween(HIERARCHICAL_DURATION, easing = defaultEasing)
    ) + fadeOut(animationSpec = tween((HIERARCHICAL_DURATION * 0.45f).toInt()))

    /** Parent screen re-enters from the left when back is pressed. */
    val hierarchicalPopEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { -(it / 3) },
        animationSpec = tween(HIERARCHICAL_POP_DURATION, easing = defaultEasing)
    ) + fadeIn(animationSpec = tween((HIERARCHICAL_POP_DURATION * 0.45f).toInt()))

    /** Detail screen exits to the right when back is pressed. */
    val hierarchicalPopExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(HIERARCHICAL_POP_DURATION, easing = defaultEasing)
    ) + fadeOut(animationSpec = tween((HIERARCHICAL_POP_DURATION * 0.35f).toInt()))

    // ═══════════════════════════════════════════════════════════════════════
    //  3. Player expand / collapse  (MiniPlayer ↔ Full Player)
    // ═══════════════════════════════════════════════════════════════════════

    val playerEnter: EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(PLAYER_DURATION, easing = defaultEasing)
    ) + fadeIn(animationSpec = tween((PLAYER_DURATION * 0.30f).toInt()))

    val playerExitBehind: ExitTransition = fadeOut(animationSpec = tween(1))

    val playerPopExit: ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(PLAYER_EXIT_DURATION, easing = defaultEasing)
    ) + fadeOut(animationSpec = tween((PLAYER_EXIT_DURATION * 0.40f).toInt()))

    val playerPopEnterBehind: EnterTransition = fadeIn(animationSpec = tween(1))
}
