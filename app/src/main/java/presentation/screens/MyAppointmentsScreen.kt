package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.MyAppointmentsViewModel
import presentation.viewmodel.SessionState

@Composable
fun MyAppointmentsScreen(
    authViewModel: AuthViewModel,
    myAppointmentsViewModel: MyAppointmentsViewModel = viewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val appointments by myAppointmentsViewModel.appointments.collectAsState()
    val isLoading by myAppointmentsViewModel.isLoading.collectAsState()
    val error by myAppointmentsViewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Активные", "История")

    // Загружаем записи при открытии экрана
    LaunchedEffect(sessionState) {
        val clientId = when (val s = sessionState) {
            is SessionState.AuthenticatedClient -> s.user.id
            is SessionState.AuthenticatedAdmin -> s.user.id
            else -> null
        }
        clientId?.let { myAppointmentsViewModel.loadAppointments(it) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Ошибка: $error", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val filtered = when (selectedTab) {
                    0 -> appointments.filter { it.status == "active" }
                    else -> appointments.filter { it.status != "active" }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Записей нет", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { appointment ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = appointment.serviceName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${appointment.date}  ${appointment.time}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${appointment.duration} мин · ${
                                            when (appointment.status) {
                                                "active" -> "Активна"
                                                "cancelled" -> "Отменена"
                                                "completed" -> "Завершена"
                                                else -> appointment.status
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (appointment.status) {
                                            "active" -> MaterialTheme.colorScheme.primary
                                            "cancelled" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
