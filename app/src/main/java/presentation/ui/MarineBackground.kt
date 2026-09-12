package presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.project.ui.theme.BrightTurquoise
import com.example.project.ui.theme.DeepOcean
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
                DeepOcean,
                Color(0xFF04122A),
                Color(0xFF082A40),
                Color(0xFF103A55)
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1800f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEAF7FA),
                Color(0xFFD4EEF5),
                Color(0xFFB8E6F0),
                Color(0xFFE0F2F7)
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
        // Декоративные элементы глубины океана (пузыри / световые пятна)
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            // Большое мягкое пятно бирюзы в верхней части
            drawCircle(
                color = Color(0xFF48CAE4).copy(alpha = 0.35f),
                radius = size.width * 0.9f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.1f)
            )
            // Второе пятно — ниже, более прозрачное
            drawCircle(
                color = Color(0xFF00B4D8).copy(alpha = 0.3f),
                radius = size.width * 0.85f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.75f)
            )
        }
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
