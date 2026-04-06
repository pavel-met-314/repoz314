package presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.AppointmentRepository

class MyAppointmentsViewModel(
    private val repository: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentClientId: String? = null

    fun loadAppointments(clientId: String) {
        currentClientId = clientId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.autoCompleteOldAppointments(clientId)
                _appointments.value = repository.getClientAppointments(clientId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        val clientId = currentClientId ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                repository.autoCompleteOldAppointments(clientId)
                _appointments.value = repository.getClientAppointments(clientId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка обновления"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

