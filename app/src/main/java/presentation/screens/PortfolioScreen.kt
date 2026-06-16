package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    selectedItem?.let { item ->
        PortfolioPhotoDialog(
            portfolio = item,
            onDismiss = { selectedItem = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = "Портфолио",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Работы нашего салона",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            portfolio.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Фото работ пока не добавлены",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortfolioGridItem(
    portfolio: Portfolio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RemoteImage(
                url = portfolio.imageUrl,
                contentDescription = portfolio.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (portfolio.caption.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = portfolio.caption,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 2
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
            shape = MaterialTheme.shapes.extraLarge,
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
                )
                if (portfolio.caption.isNotEmpty()) {
                    Text(
                        text = portfolio.caption,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 12.dp, bottom = 8.dp)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}
