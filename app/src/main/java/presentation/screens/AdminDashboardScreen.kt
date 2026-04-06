package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Appointment
import presentation.screens.admin.AdminAppointmentsScreen
import presentation.screens.admin.AdminClientsScreen
import presentation.screens.admin.AdminScheduleScreen
import presentation.screens.admin.AdminServicesScreen
import presentation.viewmodel.AdminViewModel
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.*

// Разделы нижней навигации Админки
private enum class AdminSection { DASHBOARD, APPOINTMENTS, SCHEDULE, CLIENTS, SERVICES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel = viewModel()
) {
    var currentSection by remember { mutableStateOf(AdminSection.DASHBOARD) }

    // Для дочерних экранов с кнопкой Назад
    when (currentSection) {
        AdminSection.APPOINTMENTS -> {
            AdminAppointmentsScreen(
                adminViewModel = adminViewModel,
                onBack = { currentSection = AdminSection.DASHBOARD }
            )
            return
        }
        AdminSection.SCHEDULE -> {
            AdminScheduleScreen(
                adminViewModel = adminViewModel,
                onBack = { currentSection = AdminSection.DASHBOARD }
            )
            return
        }
        AdminSection.CLIENTS -> {
            AdminClientsScreen(
                adminViewModel = adminViewModel,
                onBack = { currentSection = AdminSection.DASHBOARD }
            )
            return
        }
        AdminSection.SERVICES -> {
            AdminServicesScreen(
                adminViewModel = adminViewModel,
                onBack = { currentSection = AdminSection.DASHBOARD }
            )
            return
        }
        else -> Unit
    }

    // ─── Главный Dashboard ───────────────────────────────────────────────────
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.APPOINTMENTS },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Записи") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.SCHEDULE },
                    icon = { Icon(Icons.Default.CalendarMonth, null) },
                    label = { Text("Расписание") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.CLIENTS },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Клиенты") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.SERVICES },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Услуги") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
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

            // Карточки быстрого перехода
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Записи на сегодня",
                    value = "${todayAppointments.size}",
                    onClick = { currentSection = AdminSection.APPOINTMENTS }
                )
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Расписание",
                    value = "→",
                    onClick = { currentSection = AdminSection.SCHEDULE }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Клиенты",
                    value = "→",
                    onClick = { currentSection = AdminSection.CLIENTS }
                )
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Услуги",
                    value = "→",
                    onClick = { currentSection = AdminSection.SERVICES }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Ближайшие записи сегодня", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)

                todayAppointments.isEmpty() -> Text(
                    "На сегодня записей нет",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(todayAppointments.sortedBy { it.time }) { appointment ->
                        DashboardAppointmentCard(
                            appointment = appointment,
                            onCancel = { adminViewModel.cancelAppointment(appointment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DashboardAppointmentCard(
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
                Text(text = appointment.serviceName, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "👤 ${appointment.clientName}", style = MaterialTheme.typography.bodyMedium)
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
