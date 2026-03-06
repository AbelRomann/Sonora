package com.example.reproductor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.reproductor.presentation.components.MiniPlayer
import com.example.reproductor.presentation.navigation.NavGraph
import com.example.reproductor.presentation.navigation.Screen
import com.example.reproductor.ui.theme.AccentLime
import com.example.reproductor.ui.theme.NavInactive
import com.example.reproductor.ui.theme.PlayerBackground
import com.example.reproductor.ui.theme.ReproductorTheme
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var hasPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw content behind system bars
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make status bar icons white (light) for dark background
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false  // white icons on dark bg
        windowInsetsController.isAppearanceLightNavigationBars = false
        checkAndRequestPermission()

        setContent {
            ReproductorTheme {
                if (hasPermission) MusicPlayerApp() else PermissionScreen(onRequestPermission = ::checkAndRequestPermission)
            }
        }
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
}

@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showPlayer = currentRoute == Screen.Player.route

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (!showPlayer) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    MiniPlayer(onExpand = { navController.navigate(Screen.Player.route) })
                    BottomNavigationBar(currentRoute = currentRoute, navController = navController)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            NavGraph(navController = navController, onNavigateToPlayer = { navController.navigate(Screen.Player.route) })
        }
    }
}

@Composable
private fun BottomNavigationBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar(
        containerColor = PlayerBackground,
        contentColor = NavInactive,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigateSingleTopTo(Screen.Home.route) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (currentRoute == Screen.Home.route) AccentLime else NavInactive
                )
            },
            label = {
                Text(
                    "Inicio",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = if (currentRoute == Screen.Home.route) AccentLime else NavInactive
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AccentLime.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Library.route,
            onClick = { navController.navigateSingleTopTo(Screen.Library.route) },
            icon = {
                Icon(
                    Icons.Default.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (currentRoute == Screen.Library.route) AccentLime else NavInactive
                )
            },
            label = {
                Text(
                    "Biblioteca",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = if (currentRoute == Screen.Library.route) AccentLime else NavInactive
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AccentLime.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Playlists.route,
            onClick = { navController.navigateSingleTopTo(Screen.Playlists.route) },
            icon = {
                Icon(
                    Icons.Default.GridView,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (currentRoute == Screen.Playlists.route) AccentLime else NavInactive
                )
            },
            label = {
                Text(
                    "Playlists",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = if (currentRoute == Screen.Playlists.route) AccentLime else NavInactive
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AccentLime.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Artists.route,
            onClick = { navController.navigateSingleTopTo(Screen.Artists.route) },
            icon = {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (currentRoute == Screen.Artists.route) AccentLime else NavInactive
                )
            },
            label = {
                Text(
                    "Artistas",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = if (currentRoute == Screen.Artists.route) AccentLime else NavInactive
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = AccentLime.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = "Permiso requerido", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Necesitamos acceso a tu audio para construir tu biblioteca musical.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequestPermission) {
                Text("Conceder acceso")
            }
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(graph.startDestinationId) { saveState = true }
    }
}
