package com.example.reproductor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = PurpleAccent,
    tertiary = PinkAccent,
    background = Night,
    surface = SurfaceDark,
    surfaceVariant = SurfaceSoftDark,
    onPrimary = Night,
    onSecondary = Night,
    onTertiary = Night,
    onBackground = LightBackground,
    onSurface = LightBackground,
    onSurfaceVariant = LightSurfaceSoft,
    primaryContainer = ColorTokens.primaryContainerDark,
    onPrimaryContainer = LightBackground
)

private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.primaryLight,
    secondary = PurpleAccent,
    tertiary = PinkAccent,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceSoft,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onTertiary = LightSurface,
    onBackground = Night,
    onSurface = Midnight,
    onSurfaceVariant = SurfaceDark,
    primaryContainer = ColorTokens.primaryContainerLight,
    onPrimaryContainer = Midnight
)

private object ColorTokens {
    val primaryLight = Midnight
    val primaryContainerDark = Midnight
    val primaryContainerLight = Color(0xFFD8E3FF)
}

@Composable
fun ReproductorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
