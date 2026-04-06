package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Portfolio
import presentation.ui.RemoteImage
import presentation.viewmodel.PortfolioViewModel

@Composable
fun PortfolioScreen(
    portfolioViewModel: PortfolioViewModel = viewModel()
) {
    val portfolio by portfolioViewModel.portfolio.collectAsState()
    val isLoading by portfolioViewModel.isLoading.collectAsState()

    var selectedItem by remember { mutableStateOf<Portfolio?>(null) }

    LaunchedEffect(Unit) {
        portfolioViewModel.loadPortfolio()
    }

    // Диалог полноэкранного просмотра фото
    selectedItem?.let { item ->
        PortfolioPhotoDialog(
            portfolio = item,
            onDismiss = { selectedItem = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Портфолио",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            portfolio.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Фото работ пока не добавлены",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(portfolio) { item ->
                    PortfolioGridItem(
                        portfolio = item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioGridItem(
    portfolio: Portfolio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RemoteImage(
                url = portfolio.imageUrl,
                contentDescription = portfolio.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Подпись снизу
            if (portfolio.caption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = portfolio.caption,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioPhotoDialog(
    portfolio: Portfolio,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column {
                RemoteImage(
                    url = portfolio.imageUrl,
                    contentDescription = portfolio.caption,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                if (portfolio.caption.isNotEmpty()) {
                    Text(
                        text = portfolio.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp, bottom = 8.dp)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}
