package presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Appointment
import presentation.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val selectedDateAppointments by adminViewModel.selectedDateAppointments.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()
    val error by adminViewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(successMessage!!)
            adminViewModel.clearMessages()
        }
    }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            adminViewModel.clearMessages()
        }
    }

    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val displayFullFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

    // Генерируем даты: 7 дней назад + сегодня + 30 дней вперёд
    val dates: List<Calendar> = remember {
        (-7..30).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    var selectedDayIndex by remember {
        // По умолчанию выбираем сегодня (индекс 7 — т.к. начинаем с -7)
        mutableStateOf(7)
    }

    val selectedCalendar = dates[selectedDayIndex]
    val selectedDateStr = dbFormatter.format(selectedCalendar.time)
    val selectedDateDisplay = displayFullFormatter.format(selectedCalendar.time)

    LaunchedEffect(selectedDateStr) {
        adminViewModel.loadAppointmentsForDate(selectedDateStr)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все записи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Горизонтальный скролл по датам
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dates.size) { index ->
                    val cal = dates[index]
                    val isSelected = index == selectedDayIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDayIndex = index },
                        label = {
                            Text(displayFormatter.format(cal.time))
                        }
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = selectedDateDisplay.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                selectedDateAppointments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "На этот день записей нет",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedDateAppointments.sortedBy { it.time }) { appointment ->
                            AdminAppointmentCard(
                                appointment = appointment,
                                onCancel = {
                                    adminViewModel.cancelAppointment(appointment.id, selectedDateStr)
                                },
                                onComplete = {
                                    adminViewModel.completeAppointment(appointment.id, selectedDateStr)
                                }
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
    onCancel: () -> Unit,
    onComplete: () -> Unit
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
                val statusColor = when (appointment.status) {
                    "active" -> MaterialTheme.colorScheme.primary
                    "cancelled" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                Text(
                    text = when (appointment.status) {
                        "active" -> "Активна"
                        "cancelled" -> "Отменена"
                        "completed" -> "Завершена"
                        else -> appointment.status
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = appointment.serviceName, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "👤 ${appointment.clientName}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "📞 ${appointment.clientPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "⏱ ${appointment.duration} мин",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (appointment.status == "active") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onComplete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Завершить")
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отменить")
                    }
                }
            }
        }
    }
}

