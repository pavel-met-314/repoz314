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
    primaryContainer = Color(0xFFCCDFEA),
    onPrimaryContainer = DeepNavy,
    secondary = Turquoise,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8EBF5),
    onSecondaryContainer = Color(0xFF003547),
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = SandAccent,
    onTertiaryContainer = Color(0xFF5C2E00),
    background = OceanMist,
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SeaFoam,
    onSurfaceVariant = Color(0xFF3F4A52),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SeaFoam,
    surfaceContainer = Color(0xFFEEF6F9),
    surfaceContainerHigh = Color(0xFFE4F0F5),
    surfaceContainerHighest = Color(0xFFDCEEF5),
    outline = Color(0xFF90A4AE),
    outlineVariant = Color(0xFFC5D5DE),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrightTurquoise,
    onPrimary = NightOcean,
    primaryContainer = Color(0xFF1A4A62),
    onPrimaryContainer = Color(0xFFB8EBF5),
    secondary = Turquoise,
    onSecondary = NightOcean,
    secondaryContainer = Color(0xFF004D66),
    onSecondaryContainer = Color(0xFFB8EBF5),
    tertiary = LightCoral,
    onTertiary = Color(0xFF561414),
    tertiaryContainer = Color(0xFF5C2020),
    onTertiaryContainer = Color(0xFFFFDAD4),
    background = NightOcean,
    onBackground = Color(0xFFE2E2E5),
    surface = DarkNavy,
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = DeepSea,
    onSurfaceVariant = Color(0xFFB8C8D4),
    surfaceContainerLowest = NightOcean,
    surfaceContainerLow = Color(0xFF122035),
    surfaceContainer = Color(0xFF1A2D45),
    surfaceContainerHigh = MoonlitFoam,
    surfaceContainerHighest = Color(0xFF2E4A62),
    outline = Color(0xFF6B8494),
    outlineVariant = Color(0xFF3D5566),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = ParikmariumShapes,
        content = content
    )
}
