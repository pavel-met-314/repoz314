package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Appointment
import presentation.viewmodel.AdminViewModel
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel = viewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val todayAppointments by adminViewModel.todayAppointments.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()

    val adminName = when (val s = sessionState) {
        is SessionState.AuthenticatedAdmin -> s.user.name
        else -> "Администратор"
    }

    LaunchedEffect(Unit) {
        adminViewModel.loadTodayAppointments()
    }

    val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Панель управления") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Приветствие
            Text(
                text = "Добро пожаловать, $adminName!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Сегодня: $today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Карточка "Записи на сегодня"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Записи на сегодня",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${todayAppointments.size} записей",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Список на сегодня", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                }
                todayAppointments.isEmpty() -> {
                    Text(
                        "На сегодня записей нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(todayAppointments) { appointment ->
                            AdminAppointmentCard(
                                appointment = appointment,
                                onCancel = { adminViewModel.cancelAppointment(appointment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAppointmentCard(
    appointment: Appointment,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "👤 ${appointment.clientName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "📞 ${appointment.clientPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отменить запись")
            }
        }
    }
}
