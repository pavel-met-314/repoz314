package com.example.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,
    onPrimary = Color.White,
    primaryContainer = SeaFoam,
    onPrimaryContainer = NavyBlue,
    secondary = Turquoise,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF00344A),
    tertiary = Coral,
    onTertiary = Color.White,
    background = Color(0xFFF5FBFD),
    onBackground = Color(0xFF1A1C1E),
    surface = SeaFoam,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDCEEF5),
    error = Color(0xFFBA1A1A)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrightTurquoise,
    onPrimary = NightOcean,
    primaryContainer = Color(0xFF003547),
    onPrimaryContainer = BrightTurquoise,
    secondary = Turquoise,
    onSecondary = NightOcean,
    secondaryContainer = Color(0xFF00344A),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = LightCoral,
    onTertiary = Color(0xFF561414),
    background = NightOcean,
    onBackground = Color(0xFFE2E2E5),
    surface = DarkNavy,
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = Color(0xFF1E3A52),
    error = Color(0xFFFFB4AB)
)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}