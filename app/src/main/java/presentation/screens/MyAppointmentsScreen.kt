package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import presentation.ui.AppointmentCard
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.MyAppointmentsViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    val displayDateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru")) }

    LaunchedEffect(sessionState) {
        val clientId = when (val s = sessionState) {
            is SessionState.AuthenticatedClient -> s.user.id
            is SessionState.AuthenticatedAdmin -> s.user.id
            else -> null
        }
        clientId?.let { myAppointmentsViewModel.loadAppointments(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Мои записи",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    text = if (selectedTab == 0) "Нет активных записей" else "История пуста",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedTab == 0) {
                                        "Запишитесь на услугу в разделе «Услуги»"
                                    } else {
                                        "Завершённые и отменённые записи появятся здесь"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { appointment ->
                                val displayDate = try {
                                    val parsed = dbDateFormat.parse(appointment.date)
                                    if (parsed != null) displayDateFormat.format(parsed) else appointment.date
                                } catch (_: Exception) {
                                    appointment.date
                                }
                                AppointmentCard(
                                    appointment = appointment,
                                    displayDate = displayDate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
