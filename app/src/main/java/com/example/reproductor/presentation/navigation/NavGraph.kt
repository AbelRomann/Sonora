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
import com.example.reproductor.presentation.screens.player.PlayerScreen
import com.example.reproductor.presentation.screens.search.SearchScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Player : Screen("player")
    object Search : Screen("search")
    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
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
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                onNavigateToAlbum = { albumId ->
                    navController.navigate(Screen.Album.createRoute(albumId))
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToPlayer = onNavigateToPlayer,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Player.route) {
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