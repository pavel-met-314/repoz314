package presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.ClientHistory
import presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val clientHistories by adminViewModel.clientHistories.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedClient by remember { mutableStateOf<ClientHistory?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.loadClientHistories()
    }

    if (selectedClient != null) {
        ClientDetailScreen(
            client = selectedClient!!,
            onBack = { selectedClient = null },
            onSaveNote = { note ->
                adminViewModel.saveClientNote(selectedClient!!.clientId, note)
            },
            successMessage = successMessage,
            onClearMessage = { adminViewModel.clearMessages() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Клиенты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск по имени или телефону") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    val filtered = if (searchQuery.isBlank()) clientHistories
                    else clientHistories.filter {
                        it.clientName.contains(searchQuery, ignoreCase = true) ||
                                it.clientPhone.contains(searchQuery)
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Клиентов пока нет",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { client ->
                                ClientCard(
                                    client = client,
                                    onClick = { selectedClient = client }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientCard(client: ClientHistory, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = client.clientName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = client.clientPhone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "${client.totalVisits} визитов",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDetailScreen(
    client: ClientHistory,
    onBack: () -> Unit,
    onSaveNote: (String) -> Unit,
    successMessage: String?,
    onClearMessage: () -> Unit
) {
    var noteText by remember(client.clientId) {
        mutableStateOf(client.notes.lastOrNull()?.note ?: "")
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage)
            onClearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client.clientName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Основная информация
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Имя", client.clientName)
                        InfoRow("Телефон", client.clientPhone)
                        InfoRow("Всего визитов", client.totalVisits.toString())
                    }
                }
            }

            // История визитов
            item {
                Text("История визитов", style = MaterialTheme.typography.titleSmall)
            }

            if (client.notes.isEmpty()) {
                item {
                    Text(
                        "История пуста",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                items(client.notes.reversed()) { visit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${visit.date} · ${visit.serviceName}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (visit.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = visit.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Поле для новой заметки
            item {
                Text("Заметка к последнему визиту", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Например: любит покороче") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 5
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSaveNote(noteText) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить заметку")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

