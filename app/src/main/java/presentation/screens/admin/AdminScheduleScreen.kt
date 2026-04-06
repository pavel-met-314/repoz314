package presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Block
import domain.model.Schedule
import presentation.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val schedule by adminViewModel.schedule.collectAsState()
    val blocks by adminViewModel.blocks.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()
    val error by adminViewModel.error.collectAsState()

    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
    val displayFullFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

    val dates: List<Calendar> = remember {
        (0..30).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    var selectedDayIndex by remember { mutableStateOf(0) }
    val selectedCalendar = dates[selectedDayIndex]
    val selectedDateStr = dbFormatter.format(selectedCalendar.time)
    val selectedDateDisplay = displayFullFormatter.format(selectedCalendar.time)

    // Локальные поля для редактирования расписания
    var isWorkingDay by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var hasBreak by remember { mutableStateOf(false) }
    var breakStart by remember { mutableStateOf("13:00") }
    var breakEnd by remember { mutableStateOf("14:00") }

    // Диалог добавления блока
    var showBlockDialog by remember { mutableStateOf(false) }

    // При выборе новой даты — загружаем расписание и блоки
    LaunchedEffect(selectedDateStr) {
        adminViewModel.loadSchedule(selectedDateStr)
        adminViewModel.loadBlocks(selectedDateStr)
    }

    // Синхронизируем поля с загруженным расписанием
    LaunchedEffect(schedule) {
        val s = schedule
        if (s != null && s.date == selectedDateStr) {
            isWorkingDay = s.isWorkingDay
            startTime = s.startTime ?: "09:00"
            endTime = s.endTime ?: "18:00"
            hasBreak = s.hasBreak
            breakStart = s.breakStart ?: "13:00"
            breakEnd = s.breakEnd ?: "14:00"
        } else if (s == null) {
            isWorkingDay = false
            startTime = "09:00"
            endTime = "18:00"
            hasBreak = false
            breakStart = "13:00"
            breakEnd = "14:00"
        }
    }

    // Snackbar
    LaunchedEffect(successMessage) {
        if (successMessage != null) adminViewModel.clearMessages()
    }

    if (showBlockDialog) {
        AddBlockDialog(
            date = selectedDateStr,
            onDismiss = { showBlockDialog = false },
            onSave = { block ->
                adminViewModel.saveBlock(block)
                showBlockDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расписание") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Выбор даты
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dates.size) { index ->
                        val cal = dates[index]
                        FilterChip(
                            selected = index == selectedDayIndex,
                            onClick = { selectedDayIndex = index },
                            label = { Text(displayFormatter.format(cal.time)) }
                        )
                    }
                }
                HorizontalDivider()
            }

            // Заголовок даты
            item {
                Text(
                    text = selectedDateDisplay.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Переключатель рабочий/выходной
            item {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Рабочий день", style = MaterialTheme.typography.titleSmall)
                            Switch(checked = isWorkingDay, onCheckedChange = { isWorkingDay = it })
                        }

                        if (isWorkingDay) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Время работы
                            Text("Время работы", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = startTime,
                                    onValueChange = { startTime = it },
                                    label = { Text("Начало") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = endTime,
                                    onValueChange = { endTime = it },
                                    label = { Text("Конец") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Переключатель перерыва
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Есть перерыв", style = MaterialTheme.typography.labelMedium)
                                Switch(checked = hasBreak, onCheckedChange = { hasBreak = it })
                            }

                            if (hasBreak) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = breakStart,
                                        onValueChange = { breakStart = it },
                                        label = { Text("Начало") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = breakEnd,
                                        onValueChange = { breakEnd = it },
                                        label = { Text("Конец") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                adminViewModel.saveSchedule(
                                    Schedule(
                                        date = selectedDateStr,
                                        isWorkingDay = isWorkingDay,
                                        startTime = if (isWorkingDay) startTime else null,
                                        endTime = if (isWorkingDay) endTime else null,
                                        hasBreak = hasBreak && isWorkingDay,
                                        breakStart = if (hasBreak && isWorkingDay) breakStart else null,
                                        breakEnd = if (hasBreak && isWorkingDay) breakEnd else null
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сохранить расписание")
                        }

                        successMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.secondary)
                        }
                        error?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Личные блоки
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Личные блоки", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showBlockDialog = true }) {
                        Text("+ Добавить")
                    }
                }
            }

            if (blocks.isEmpty()) {
                item {
                    Text(
                        "Блоков нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(blocks) { block ->
                    BlockCard(
                        block = block,
                        onDelete = { adminViewModel.deleteBlock(block.id, selectedDateStr) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockCard(block: Block, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = block.reason.ifEmpty { "Без названия" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${block.startTime} – ${block.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddBlockDialog(
    date: String,
    onDismiss: () -> Unit,
    onSave: (Block) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("12:00") }
    var endTime by remember { mutableStateOf("13:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить блок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Название (например, Стоматолог)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Начало") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Конец") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(Block(id = "", date = date, startTime = startTime, endTime = endTime, reason = reason))
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

