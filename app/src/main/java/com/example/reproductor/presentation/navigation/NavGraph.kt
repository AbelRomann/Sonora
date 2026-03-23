package com.example.reproductor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reproductor.presentation.screens.album.AlbumScreen
import com.example.reproductor.presentation.screens.artists.ArtistDetailScreen
import com.example.reproductor.presentation.screens.artists.ArtistsScreen
import com.example.reproductor.presentation.screens.home.HomeScreen
import com.example.reproductor.presentation.screens.library.LibraryScreen
import com.example.reproductor.presentation.screens.library.PlaylistDetailScreen
import com.example.reproductor.presentation.screens.player.PlayerScreen
import com.example.reproductor.presentation.screens.playlists.PlaylistsScreen
import com.example.reproductor.presentation.screens.search.SearchScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Playlists : Screen("playlists")
    object Artists : Screen("artists")
    object Player : Screen("player")
    object Search : Screen("search")
    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object ArtistDetail : Screen("artist_detail/{artistName}") {
        fun createRoute(artistName: String): String {
            val encoded = URLEncoder.encode(artistName, "UTF-8")
            return "artist_detail/$encoded"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onNavigateToPlayer: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        // ── Default transitions (hierarchical) ──────────────────────────────
        // Individual routes override these when needed (top-level, player).
        enterTransition    = { NavAnimations.hierarchicalEnter },
        exitTransition     = { NavAnimations.hierarchicalExit },
        popEnterTransition = { NavAnimations.hierarchicalPopEnter },
        popExitTransition  = { NavAnimations.hierarchicalPopExit }
    ) {

        // ═════════════════════════════════════════════════════════════════════
        //  TOP-LEVEL ROUTES  —  direction-aware slide between peer tabs,
        //  plus hierarchical exit/popEnter when a detail child is pushed/popped.
        //
        //  The lambda receives `initialState` and `targetState` (NavBackStackEntry),
        //  so we can compare their tab indices to determine slide direction.
        // ═════════════════════════════════════════════════════════════════════

        composable(
            route = Screen.Home.route,
            enterTransition    = { NavAnimations.topLevelEnter(initialState, targetState) },
            exitTransition     = {
                // If target is a peer tab → direction-aware; if child detail → hierarchical
                if (TAB_ORDER.contains(targetState.destination?.route))
                    NavAnimations.topLevelExit(initialState, targetState)
                else
                    NavAnimations.hierarchicalExit
            },
            popEnterTransition = { NavAnimations.hierarchicalPopEnter },
            popExitTransition  = { NavAnimations.topLevelPopExit }
        ) {
            HomeScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) }
            )
        }

        composable(
            route = Screen.Library.route,
            enterTransition    = { NavAnimations.topLevelEnter(initialState, targetState) },
            exitTransition     = {
                if (TAB_ORDER.contains(targetState.destination?.route))
                    NavAnimations.topLevelExit(initialState, targetState)
                else
                    NavAnimations.hierarchicalExit
            },
            popEnterTransition = { NavAnimations.hierarchicalPopEnter },
            popExitTransition  = { NavAnimations.topLevelPopExit }
        ) {
            LibraryScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(
            route = Screen.Playlists.route,
            enterTransition    = { NavAnimations.topLevelEnter(initialState, targetState) },
            exitTransition     = {
                if (TAB_ORDER.contains(targetState.destination?.route))
                    NavAnimations.topLevelExit(initialState, targetState)
                else
                    NavAnimations.hierarchicalExit
            },
            popEnterTransition = { NavAnimations.hierarchicalPopEnter },
            popExitTransition  = { NavAnimations.topLevelPopExit }
        ) {
            PlaylistsScreen(
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(
            route = Screen.Artists.route,
            enterTransition    = { NavAnimations.topLevelEnter(initialState, targetState) },
            exitTransition     = {
                if (TAB_ORDER.contains(targetState.destination?.route))
                    NavAnimations.topLevelExit(initialState, targetState)
                else
                    NavAnimations.hierarchicalExit
            },
            popEnterTransition = { NavAnimations.hierarchicalPopEnter },
            popExitTransition  = { NavAnimations.topLevelPopExit }
        ) {
            ArtistsScreen(
                onNavigateToArtistDetail = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                }
            )
        }

        // ═════════════════════════════════════════════════════════════════════
        //  HIERARCHICAL ROUTES  —  uses NavHost default (no override needed)
        // ═════════════════════════════════════════════════════════════════════

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
            // Uses default hierarchical transitions from NavHost
        ) {
            PlaylistDetailScreen(
                playlistId = it.arguments?.getLong("playlistId") ?: 0L,
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() },
                onDeletePlaylist = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(
                navArgument("artistName") { type = NavType.StringType }
            )
            // Uses default hierarchical transitions from NavHost
        ) {
            val encodedName = it.arguments?.getString("artistName") ?: ""
            val artistName = URLDecoder.decode(encodedName, "UTF-8")
            ArtistDetailScreen(
                artistName = artistName,
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Search.route
            // Uses default hierarchical transitions from NavHost
        ) {
            SearchScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Album.route,
            arguments = listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                }
            )
            // Uses default hierarchical transitions from NavHost
        ) {
            AlbumScreen(
                albumId = it.arguments?.getString("albumId")?.toLongOrNull() ?: 0L,
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ═════════════════════════════════════════════════════════════════════
        //  PLAYER ROUTE  —  vertical slide up / down
        // ═════════════════════════════════════════════════════════════════════

        composable(
            route = Screen.Player.route,
            enterTransition  = { NavAnimations.playerEnter },
            exitTransition   = { NavAnimations.playerPopExit },
            popEnterTransition = { NavAnimations.playerPopEnterBehind },
            popExitTransition  = { NavAnimations.playerPopExit }
        ) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
