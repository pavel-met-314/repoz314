package presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.project.ui.theme.BrightTurquoise
import com.example.project.ui.theme.NavyBlue
import com.example.project.ui.theme.NightOcean
import com.example.project.ui.theme.OceanMist
import com.example.project.ui.theme.SeaFoam
import com.example.project.ui.theme.Turquoise

@Composable
fun MarineGradientBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val brush = if (darkTheme) {
        Brush.linearGradient(
            colors = listOf(
                NightOcean,
                Color(0xFF0F2840),
                Color(0xFF153550)
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1800f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                OceanMist,
                SeaFoam,
                Color(0xFFD4EEF5),
                Color(0xFFEAF7FA)
            ),
            start = Offset(0f, 0f),
            end = Offset(800f, 1600f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}

@Composable
fun MarineAccentBrush(darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()): Brush {
    return if (darkTheme) {
        Brush.horizontalGradient(listOf(BrightTurquoise, Turquoise))
    } else {
        Brush.horizontalGradient(listOf(NavyBlue, Turquoise))
    }
}
