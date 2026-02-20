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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.reproductor.presentation.components.MiniPlayer
import com.example.reproductor.presentation.navigation.NavGraph
import com.example.reproductor.presentation.navigation.Screen
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
        checkAndRequestPermission()

        setContent {
            ReproductorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasPermission) MusicPlayerApp() else PermissionScreen(onRequestPermission = ::checkAndRequestPermission)
                }
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
        bottomBar = {
            if (!showPlayer) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    MiniPlayer(onExpand = { navController.navigate(Screen.Player.route) })
                    BottomNavigationBar(currentRoute = currentRoute, navController = navController)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavGraph(navController = navController, onNavigateToPlayer = { navController.navigate(Screen.Player.route) })
        }
    }
}

@Composable
private fun BottomNavigationBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { navController.navigateSingleTopTo(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Library.route,
            onClick = { navController.navigateSingleTopTo(Screen.Library.route) },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null) },
            label = { Text("Biblioteca") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = { navController.navigateSingleTopTo(Screen.Search.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Buscar") }
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
