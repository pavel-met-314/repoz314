package presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import presentation.ui.AdminAppointmentCard
import presentation.ui.MarineFilterChip
import presentation.ui.ParikmariumTopAppBar
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
    val displayFullFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ru"))

    val dates: List<Calendar> = remember {
        (-7..30).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
        }
    }

    var selectedDayIndex by remember { mutableStateOf(7) }

    val selectedCalendar = dates[selectedDayIndex]
    val selectedDateStr = dbFormatter.format(selectedCalendar.time)
    val selectedDateDisplay = displayFullFormatter.format(selectedCalendar.time)

    LaunchedEffect(selectedDateStr) {
        adminViewModel.loadAppointmentsForDate(selectedDateStr)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ParikmariumTopAppBar(title = "Все записи", onBack = onBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dates.size) { index ->
                    val cal = dates[index]
                    MarineFilterChip(
                        selected = index == selectedDayIndex,
                        label = displayFormatter.format(cal.time),
                        onClick = { selectedDayIndex = index }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                text = selectedDateDisplay.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                selectedDateAppointments.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "На этот день записей нет",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
