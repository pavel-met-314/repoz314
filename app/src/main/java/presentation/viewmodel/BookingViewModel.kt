package presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Appointment
import domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.AppointmentRepository

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    object Success : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel(
    private val repository: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState

    private val _availableSlots = MutableStateFlow<List<String>>(emptyList())
    val availableSlots: StateFlow<List<String>> = _availableSlots

    private val _isLoadingSlots = MutableStateFlow(false)
    val isLoadingSlots: StateFlow<Boolean> = _isLoadingSlots

    fun loadSlots(date: String, serviceDuration: Int) {
        viewModelScope.launch {
            _isLoadingSlots.value = true
            try {
                _availableSlots.value = repository.getAvailableSlots(date, serviceDuration)
            } catch (_: Exception) {
                _availableSlots.value = emptyList()
            } finally {
                _isLoadingSlots.value = false
            }
        }
    }

    fun book(
        clientId: String,
        clientName: String,
        clientPhone: String,
        service: Service,
        date: String,
        time: String
    ) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                val appointment = Appointment(
                    id = "",
                    clientId = clientId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    serviceId = service.id,
                    serviceName = service.name,
                    date = date,
                    time = time,
                    duration = service.duration,
                    status = "active",
                    createdAt = System.currentTimeMillis()
                )
                repository.createAppointment(appointment)
                _uiState.value = BookingUiState.Success
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error(e.message ?: "Ошибка записи")
            }
        }
    }

    fun resetState() {
        _uiState.value = BookingUiState.Idle
    }
}

