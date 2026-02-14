package com.example.reproductor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reproductor.presentation.components.MiniPlayer
import com.example.reproductor.presentation.navigation.NavGraph
import com.example.reproductor.presentation.navigation.Screen
import com.example.reproductor.ui.theme.ReproductorTheme
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (hasPermission) {
                        MusicPlayerApp()
                    } else {
                        PermissionScreen(
                            onRequestPermission = { checkAndRequestPermission() }
                        )
                    }
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

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showPlayer = currentRoute == Screen.Player.route

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player (se muestra en todas las pantallas excepto en el Player completo)
                if (!showPlayer) {
                    MiniPlayer(
                        onExpand = {
                            navController.navigate(Screen.Player.route)
                        }
                    )
                }

                // Bottom Navigation
                if (!showPlayer) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onHomeClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onLibraryClick = {
                            navController.navigate(Screen.Library.route)
                        },
                        onSearchClick = {
                            navController.navigate(Screen.Search.route)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavGraph(
                navController = navController,
                onNavigateToPlayer = {
                    navController.navigate(Screen.Player.route)
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Library.route,
            onClick = onLibraryClick,
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
            label = { Text("Biblioteca") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = onSearchClick,
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Buscar") }
        )
    }
}

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permiso necesario",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Necesitamos acceso a tu música para poder reproducirla",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onRequestPermission) {
                Text("Conceder permiso")
            }
        }
    }
}


