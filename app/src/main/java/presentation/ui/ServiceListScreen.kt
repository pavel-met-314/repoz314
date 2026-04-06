package presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Service
import presentation.viewmodel.ServiceViewModel
import presentation.viewmodel.ServiceUiState

@Composable
fun ServiceListScreen(
    viewModel: ServiceViewModel = viewModel(),
    onServiceSelected: (Service) -> Unit = {}
) {
    // Вызов загрузки данных при первом отображении
    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ServiceUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ServiceUiState.Success -> {
            val services = (uiState as ServiceUiState.Success).services
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(services) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
            }
        }
        is ServiceUiState.Error -> {
            val message = (uiState as ServiceUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = "Ошибка: $message", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
