package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R
import presentation.screens.admin.AdminAppointmentsScreen
import presentation.screens.admin.AdminClientsScreen
import presentation.screens.admin.AdminPortfolioScreen
import presentation.screens.admin.AdminScheduleScreen
import presentation.screens.admin.AdminServicesScreen
import presentation.ui.AdminAppointmentCard
import presentation.viewmodel.AdminViewModel
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.PortfolioViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.*

private enum class AdminSection { DASHBOARD, APPOINTMENTS, SCHEDULE, CLIENTS, SERVICES, PORTFOLIO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel = viewModel(),
    portfolioViewModel: PortfolioViewModel = viewModel()
) {
    var currentSection by remember { mutableStateOf(AdminSection.DASHBOARD) }

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
        AdminSection.PORTFOLIO -> {
            AdminPortfolioScreen(
                portfolioViewModel = portfolioViewModel,
                onBack = { currentSection = AdminSection.DASHBOARD }
            )
            return
        }
        else -> Unit
    }

    val sessionState by authViewModel.sessionState.collectAsState()
    val todayAppointments by adminViewModel.todayAppointments.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val isRefreshing by adminViewModel.isRefreshing.collectAsState()
    val error by adminViewModel.error.collectAsState(initial = null)

    val snackbarHostState = remember { SnackbarHostState() }

    val adminName = when (val s = sessionState) {
        is SessionState.AuthenticatedAdmin -> s.user.name
        else -> "Администратор"
    }

    LaunchedEffect(Unit) {
        adminViewModel.loadTodayAppointments()
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "Ошибка")
            adminViewModel.clearMessages()
        }
    }

    val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Панель администратора",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выйти")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Главная") },
                    colors = adminNavColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.APPOINTMENTS },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Записи") },
                    colors = adminNavColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.SCHEDULE },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Расписание") },
                    colors = adminNavColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.CLIENTS },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Клиенты") },
                    colors = adminNavColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.SERVICES },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Услуги") },
                    colors = adminNavColors()
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { currentSection = AdminSection.PORTFOLIO },
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Фото") },
                    colors = adminNavColors()
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
                text = "Добро пожаловать, $adminName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Сегодня: $today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    modifier = Modifier.weight(1f),
                    title = "Записи сегодня",
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
            Text(
                text = "Записи на сегодня",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { adminViewModel.refreshTodayAppointments() },
                modifier = Modifier.weight(1f)
            ) {
                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    todayAppointments.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "На сегодня записей нет",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(todayAppointments.sortedBy { it.time }) { appointment ->
                            AdminAppointmentCard(
                                appointment = appointment,
                                onCancel = { adminViewModel.cancelAppointment(appointment.id) },
                                onComplete = { adminViewModel.completeAppointment(appointment.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun adminNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)

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
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
