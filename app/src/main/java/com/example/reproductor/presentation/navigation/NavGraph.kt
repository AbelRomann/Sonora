package com.example.reproductor.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reproductor.presentation.screens.album.AlbumScreen
import com.example.reproductor.presentation.screens.home.HomeScreen
import com.example.reproductor.presentation.screens.library.LibraryScreen
import com.example.reproductor.presentation.screens.library.PlaylistDetailScreen
import com.example.reproductor.presentation.screens.player.PlayerScreen
import com.example.reproductor.presentation.screens.search.SearchScreen
import com.example.reproductor.presentation.screens.showcase.ShowcaseScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Player : Screen("player")
    object Search : Screen("search")
    object Showcase : Screen("showcase")
    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onNavigateToPlayer: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
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
                onBackClick = { navController.popBackStack() },
                onOpenPlaylist = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
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

        composable(Screen.Showcase.route) {
            ShowcaseScreen()
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
