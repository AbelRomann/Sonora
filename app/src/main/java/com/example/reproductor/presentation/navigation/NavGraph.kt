package com.example.reproductor.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Player : Screen("player")
    object Search : Screen("search")
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
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(220)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(220)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(220)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(220)
            )
        }
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
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

        composable(
            route = Screen.Player.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(260)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(260)
                )
            }
        ) {
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
