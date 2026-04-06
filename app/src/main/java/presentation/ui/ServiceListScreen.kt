package presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Service
import presentation.viewmodel.PortfolioViewModel
import presentation.viewmodel.ServiceUiState
import presentation.viewmodel.ServiceViewModel

@Composable
fun ServiceListScreen(
    viewModel: ServiceViewModel = viewModel(),
    portfolioViewModel: PortfolioViewModel = viewModel(),
    onServiceSelected: (Service) -> Unit = {}
) {
    // Вызов загрузки данных при первом отображении
    LaunchedEffect(Unit) {
        viewModel.loadServices()
        portfolioViewModel.loadPortfolio()
    }

    val uiState by viewModel.uiState.collectAsState()
    val portfolio by portfolioViewModel.portfolio.collectAsState()

    when (uiState) {
        is ServiceUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ServiceUiState.Success -> {
            val services = (uiState as ServiceUiState.Success).services
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // ── Карусель фото ─────────────────────────────────────────────
                if (portfolio.isNotEmpty()) {
                    item {
                        PortfolioCarousel(
                            items = portfolio.map { it.imageUrl to it.caption },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ── Заголовок услуг ───────────────────────────────────────────
                item {
                    Text(
                        text = "Наши услуги",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Список услуг ──────────────────────────────────────────────
                items(services) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = service.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Цена: ${service.price} ₽", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Длительность: ${service.duration} мин", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onServiceSelected(service) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Записаться")
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
        is ServiceUiState.Error -> {
            val message = (uiState as ServiceUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Ошибка: $message", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PortfolioCarousel(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val (url, caption) = items[page]
            Box(modifier = Modifier.fillMaxSize()) {
                RemoteImage(
                    url = url,
                    contentDescription = caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Подпись внизу
                if (caption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Индикатор точек
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(items.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}
