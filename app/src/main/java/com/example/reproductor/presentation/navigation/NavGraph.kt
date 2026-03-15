package com.example.reproductor.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToSearch = { navController.navigate(Screen.Search.route) }
            )
        }

        composable(Screen.Playlists.route) {
            PlaylistsScreen(
                onNavigateToPlaylistDetail = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }

        composable(Screen.Artists.route) {
            ArtistsScreen(
                onNavigateToArtistDetail = { artistName ->
                    navController.navigate(Screen.ArtistDetail.createRoute(artistName))
                }
            )
        }

        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.LongType }
            )
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
        ) {
            val encodedName = it.arguments?.getString("artistName") ?: ""
            val artistName = URLDecoder.decode(encodedName, "UTF-8")
            ArtistDetailScreen(
                artistName = artistName,
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Player.route) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
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
        ) {
            AlbumScreen(
                albumId = it.arguments?.getString("albumId")?.toLongOrNull() ?: 0L,
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
