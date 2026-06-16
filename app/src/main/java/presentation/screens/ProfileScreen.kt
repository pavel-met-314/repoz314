package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project.ui.theme.ProjectTheme
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.SessionState

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val sessionState by authViewModel.sessionState.collectAsState()

    val userName = when (val s = sessionState) {
        is SessionState.AuthenticatedClient -> s.user.name
        is SessionState.AuthenticatedAdmin -> s.user.name
        else -> ""
    }
    val userEmail = when (val s = sessionState) {
        is SessionState.AuthenticatedClient -> s.user.email
        is SessionState.AuthenticatedAdmin -> s.user.email
        else -> ""
    }
    val userPhone = when (val s = sessionState) {
        is SessionState.AuthenticatedClient -> s.user.phone
        is SessionState.AuthenticatedAdmin -> s.user.phone
        else -> ""
    }

    ProfileScreenContent(
        userName = userName,
        userEmail = userEmail,
        userPhone = userPhone,
        onLogout = { authViewModel.logout() }
    )
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProjectTheme {
        ProfileScreenContent(
            userName = "Анна Иванова",
            userEmail = "anna@example.com",
            userPhone = "+7 900 000-00-00",
            onLogout = {}
        )
    }
}

@Composable
private fun ProfileScreenContent(
    userName: String,
    userEmail: String,
    userPhone: String,
    onLogout: () -> Unit
) {
    val initials = userName.trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifEmpty { "?" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userName.ifEmpty { "Гость" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Профиль клиента",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        ProfileInfoRow(icon = Icons.Filled.Person, label = "Имя", value = userName)
        Spacer(modifier = Modifier.height(10.dp))
        ProfileInfoRow(icon = Icons.Filled.Email, label = "Email", value = userEmail)
        Spacer(modifier = Modifier.height(10.dp))
        ProfileInfoRow(icon = Icons.Filled.Phone, label = "Телефон", value = userPhone)

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifEmpty { "—" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
