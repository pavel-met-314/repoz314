package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.theme.ProjectTheme
import presentation.viewmodel.AuthUiState
import presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (isAdmin: Boolean) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()

    val isFormValid = email.isNotBlank() && password.length >= 6

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val user = (uiState as AuthUiState.Success).user
            onLoginSuccess(user.role == "admin")
            authViewModel.resetState()
        }
    }

    LoginScreenContent(
        email = email,
        password = password,
        isAdmin = isAdmin,
        isFormValid = isFormValid,
        isLoading = uiState is AuthUiState.Loading,
        errorMessage = (uiState as? AuthUiState.Error)?.message,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onAdminChange = { isAdmin = it },
        onLoginClick = { authViewModel.login(email, password, asAdmin = isAdmin) },
        onRegisterClick = onRegisterClick
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    ProjectTheme {
        LoginScreenContent(
            email = "client@example.com",
            password = "secret",
            isAdmin = false,
            isFormValid = true,
            isLoading = false,
            errorMessage = null,
            onEmailChange = {},
            onPasswordChange = {},
            onAdminChange = {},
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Composable
private fun LoginScreenContent(
    email: String,
    password: String,
    isAdmin: Boolean,
    isFormValid: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAdminChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌊", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Морская причёска",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isAdmin) "Панель администратора" else "Запись на услуги",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isAdmin, onCheckedChange = onAdminChange)
                Text(text = "Вход как админ")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLoginClick,
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRegisterClick) {
                Text("Нет аккаунта? Зарегистрироваться")
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
