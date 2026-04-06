package presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Service
import presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val services by adminViewModel.services.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()
    val error by adminViewModel.error.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<Service?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.loadServices()
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) adminViewModel.clearMessages()
    }

    if (showDialog) {
        ServiceDialog(
            initial = editingService,
            onDismiss = {
                showDialog = false
                editingService = null
            },
            onSave = { service ->
                adminViewModel.saveService(service)
                showDialog = false
                editingService = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление услугами") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingService = null
                    showDialog = true
                },
                icon = {},
                text = { Text("+ Добавить услугу") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Сообщения об успехе/ошибке
            successMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (services.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Услуг пока нет. Добавьте первую!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(services) { service ->
                        ServiceCard(
                            service = service,
                            onEdit = {
                                editingService = service
                                showDialog = true
                            },
                            onDelete = { adminViewModel.deleteService(service.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: Service,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = service.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${service.price} ₽  ·  ${service.duration} мин",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ServiceDialog(
    initial: Service?,
    onDismiss: () -> Unit,
    onSave: (Service) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var priceStr by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var durationStr by remember { mutableStateOf(initial?.duration?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новая услуга" else "Редактировать услугу") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Цена (₽)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Длительность (мин)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceStr.toIntOrNull() ?: 0
                    val duration = durationStr.toIntOrNull() ?: 30
                    onSave(
                        Service(
                            id = initial?.id ?: "",
                            name = name.trim(),
                            price = price,
                            duration = duration
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

