package presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project.ui.theme.ProjectTheme
import domain.model.Appointment

@Composable
fun AppointmentCard(
    appointment: Appointment,
    displayDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = appointment.status)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = displayDate, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${appointment.time} · ${appointment.duration} мин",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (statusText, statusColor) = when (status) {
        "active" -> "Активна" to MaterialTheme.colorScheme.primary
        "cancelled" -> "Отменена" to MaterialTheme.colorScheme.error
        "completed" -> "Завершена" to MaterialTheme.colorScheme.secondary
        else -> status to MaterialTheme.colorScheme.onSurface
    }
    androidx.compose.material3.Surface(
        shape = MaterialTheme.shapes.small,
        color = statusColor.copy(alpha = 0.12f)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppointmentCardPreview() {
    ProjectTheme {
        AppointmentCard(
            appointment = Appointment(
                id = "1",
                serviceName = "Стрижка",
                date = "2024-05-20",
                time = "15:00",
                duration = 30,
                status = "active"
            ),
            displayDate = "20 мая 2024",
            modifier = Modifier.padding(16.dp)
        )
    }
}
