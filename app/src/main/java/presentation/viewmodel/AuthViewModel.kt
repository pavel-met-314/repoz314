package presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.AuthRepository

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// Состояние сессии при запуске приложения
sealed class SessionState {
    object Checking : SessionState()           // идёт проверка
    object Unauthenticated : SessionState()    // не авторизован → экран входа
    data class AuthenticatedClient(val user: User) : SessionState()  // клиент
    data class AuthenticatedAdmin(val user: User) : SessionState()   // админ
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Checking)
    val sessionState: StateFlow<SessionState> = _sessionState

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = SessionState.Checking
            try {
                val user = repository.getCurrentUser()
                if (user == null) {
                    _sessionState.value = SessionState.Unauthenticated
                } else if (user.role == "admin") {
                    _sessionState.value = SessionState.AuthenticatedAdmin(user)
                } else {
                    _sessionState.value = SessionState.AuthenticatedClient(user)
                }
            } catch (e: Exception) {
                _sessionState.value = SessionState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String, asAdmin: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.login(email, password)
                val isAdminUser = user.role == "admin"
                if (asAdmin != isAdminUser) {
                    repository.logout()
                    _sessionState.value = SessionState.Unauthenticated
                    _uiState.value = AuthUiState.Error(
                        if (asAdmin) {
                            "У вас нет прав администратора"
                        } else {
                            "Для входа администратора отметьте «Вход как админ»"
                        }
                    )
                    return@launch
                }
                updateSessionFromUser(user)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Ошибка входа")
            }
        }
    }

    fun register(email: String, password: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.register(email, password, name, phone)
                updateSessionFromUser(user)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    private fun updateSessionFromUser(user: User) {
        _sessionState.value = if (user.role == "admin") {
            SessionState.AuthenticatedAdmin(user)
        } else {
            SessionState.AuthenticatedClient(user)
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.Idle
        _sessionState.value = SessionState.Unauthenticated
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
