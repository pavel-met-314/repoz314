package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Профиль", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Имя", style = MaterialTheme.typography.labelSmall)
                Text(text = userName.ifEmpty { "—" }, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Email", style = MaterialTheme.typography.labelSmall)
                Text(text = userEmail.ifEmpty { "—" }, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Телефон", style = MaterialTheme.typography.labelSmall)
                Text(text = userPhone.ifEmpty { "—" }, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Выйти")
        }
    }
}


