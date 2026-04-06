package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.MyAppointmentsViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyAppointmentsScreen(
    authViewModel: AuthViewModel,
    myAppointmentsViewModel: MyAppointmentsViewModel = viewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val appointments by myAppointmentsViewModel.appointments.collectAsState()
    val isLoading by myAppointmentsViewModel.isLoading.collectAsState()
    val isRefreshing by myAppointmentsViewModel.isRefreshing.collectAsState()
    val error by myAppointmentsViewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Активные", "История")

    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ru")) }

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

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { myAppointmentsViewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ошибка загрузки",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { myAppointmentsViewModel.refresh() }) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                else -> {
                    val filtered = when (selectedTab) {
                        0 -> appointments.filter { it.status == "active" }
                        else -> appointments.filter { it.status != "active" }
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTab == 0) "📋" else "📜",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedTab == 0) "Активных записей нет" else "История пуста",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { appointment ->
                                val displayDate = try {
                                    val parsed = dbDateFormat.parse(appointment.date)
                                    if (parsed != null) displayDateFormat.format(parsed) else appointment.date
                                } catch (_: Exception) { appointment.date }

                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = appointment.serviceName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "📅 $displayDate",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "🕐 ${appointment.time}  ·  ${appointment.duration} мин",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            val (statusText, statusColor) = when (appointment.status) {
                                                "active" -> "Активна" to MaterialTheme.colorScheme.primary
                                                "cancelled" -> "Отменена" to MaterialTheme.colorScheme.error
                                                "completed" -> "Завершена" to MaterialTheme.colorScheme.secondary
                                                else -> appointment.status to MaterialTheme.colorScheme.onSurface
                                            }
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = statusColor.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = statusColor,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
        }
    }
}
