package presentation.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.theme.ProjectTheme
import com.example.project.notification.ReminderScheduler
import domain.model.Service
import presentation.ui.MarineGradientBackground
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.BookingUiState
import presentation.viewmodel.BookingViewModel
import presentation.viewmodel.SessionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    service: Service,
    onBack: () -> Unit = {},
    onBookingSuccess: () -> Unit = {},
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val sessionState by authViewModel.sessionState.collectAsState()
    val uiState by bookingViewModel.uiState.collectAsState()
    val availableSlots by bookingViewModel.availableSlots.collectAsState()
    val isLoadingSlots by bookingViewModel.isLoadingSlots.collectAsState()
    val context = LocalContext.current

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val displayFullFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val dates: List<Calendar> = remember {
        (0..13).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    val selectedCalendar = selectedDayIndex?.let { dates[it] }
    val selectedDateStr = selectedCalendar?.let { dbFormatter.format(it.time) }
    val selectedDateDisplay = selectedCalendar?.let { displayFullFormatter.format(it.time) } ?: ""

    LaunchedEffect(selectedDateStr) {
        selectedDateStr?.let { date ->
            selectedSlot = null
            bookingViewModel.loadSlots(date, service.duration)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is BookingUiState.Success) {
            val date = selectedDateStr
            val slot = selectedSlot
            if (date != null && slot != null) {
                ReminderScheduler.schedule(
                    context = context,
                    appointmentId = "${date}_${slot}_${service.id}",
                    serviceName = service.name,
                    date = date,
                    time = slot
                )
            }
            showSuccess = true
        }
    }

    if (showSuccess) {
        BookingSuccessScreen(
            service = service,
            date = selectedDateDisplay,
            time = selectedSlot ?: "",
            onGoHome = {
                bookingViewModel.resetState()
                onBookingSuccess()
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Запись") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${service.price} ₽ · ${service.duration} мин",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Выберите дату",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dates.size) { index ->
                    val cal = dates[index]
                    val isSelected = index == selectedDayIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDayIndex = index },
                        label = {
                            Text(
                                text = displayFormatter.format(cal.time),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.height(44.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedDayIndex != null) {
                Text(
                    text = "Доступное время",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                when {
                    isLoadingSlots -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    availableSlots.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "На этот день нет доступного времени",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(76.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(availableSlots) { slot ->
                                FilterChip(
                                    selected = slot == selectedSlot,
                                    onClick = { selectedSlot = slot },
                                    label = { Text(slot) },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (uiState is BookingUiState.Error) {
                Text(
                    text = (uiState as BookingUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val date = selectedDateStr ?: return@Button
                    val slot = selectedSlot ?: return@Button
                    val (clientId, clientName, clientPhone) = when (val s = sessionState) {
                        is SessionState.AuthenticatedClient -> Triple(s.user.id, s.user.name, s.user.phone)
                        is SessionState.AuthenticatedAdmin -> Triple(s.user.id, s.user.name, s.user.phone)
                        else -> return@Button
                    }
                    bookingViewModel.book(clientId, clientName, clientPhone, service, date, slot)
                },
                enabled = selectedDayIndex != null && selectedSlot != null && uiState !is BookingUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.large
            ) {
                if (uiState is BookingUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Записаться", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookingSuccessPreview() {
    ProjectTheme {
        BookingSuccessScreen(
            service = Service(id = "1", name = "Стрижка", price = 800, duration = 30),
            date = "16.06.2026",
            time = "14:00",
            onGoHome = {}
        )
    }
}

@Composable
private fun BookingSuccessScreen(
    service: Service,
    date: String,
    time: String,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current

    MarineGradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Вы записаны!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Услуга", service.name)
                    InfoRow("Дата", date)
                    InfoRow("Время", time)
                    InfoRow("Цена", "${service.price} ₽")
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("На главную")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    try {
                        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        val startDate = formatter.parse("$date $time")
                        val startMillis = startDate?.time ?: return@OutlinedButton
                        val endMillis = startMillis + service.duration * 60_000L

                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                            putExtra(CalendarContract.Events.TITLE, "Parikmarium: ${service.name}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Цена: ${service.price} ₽")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Добавить в календарь")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
