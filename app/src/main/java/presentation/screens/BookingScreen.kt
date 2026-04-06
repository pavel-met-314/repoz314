package presentation.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import domain.model.Service
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

    // Индекс выбранного дня (0 = сегодня, ..., 13)
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val displayFullFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Генерируем 14 дат начиная с сегодня
    val dates: List<Calendar> = remember {
        (0..13).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    val selectedCalendar = selectedDayIndex?.let { dates[it] }
    val selectedDateStr = selectedCalendar?.let { dbFormatter.format(it.time) }
    val selectedDateDisplay = selectedCalendar?.let { displayFullFormatter.format(it.time) } ?: ""

    // При изменении даты загружаем слоты
    LaunchedEffect(selectedDateStr) {
        selectedDateStr?.let { date ->
            selectedSlot = null
            bookingViewModel.loadSlots(date, service.duration)
        }
    }

    // Когда запись успешна — показываем экран подтверждения
    LaunchedEffect(uiState) {
        if (uiState is BookingUiState.Success) {
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
        topBar = {
            TopAppBar(
                title = { Text("Запись на услугу") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Информация об услуге
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = service.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${service.price} ₽ · ${service.duration} мин",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Выберите дату", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            // Горизонтальный список дат
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedDayIndex != null) {
                Text("Доступное время", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    isLoadingSlots -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    availableSlots.isEmpty() -> {
                        Text(
                            "На этот день нет доступного времени",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(72.dp),
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
                                    label = { Text(slot) }
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Ошибка
            if (uiState is BookingUiState.Error) {
                Text(
                    text = (uiState as BookingUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Кнопка записаться
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
                    .padding(vertical = 16.dp)
            ) {
                if (uiState is BookingUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Записаться")
                }
            }
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Вы записаны!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Услуга", service.name)
                InfoRow("Дата", date)
                InfoRow("Время", time)
                InfoRow("Цена", "${service.price} ₽")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onGoHome, modifier = Modifier.fillMaxWidth()) {
            Text("На главную")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                // Парсим дату и время для Calendar Intent
                try {
                    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    val startDate = formatter.parse("$date $time")
                    val startMillis = startDate?.time ?: return@OutlinedButton
                    val endMillis = startMillis + service.duration * 60_000L

                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                        putExtra(CalendarContract.Events.TITLE, "Запись: ${service.name}")
                        putExtra(CalendarContract.Events.DESCRIPTION, "Цена: ${service.price} ₽")
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить в календарь")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
