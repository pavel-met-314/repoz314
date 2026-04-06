package presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.AppointmentRepository
import java.text.SimpleDateFormat
import java.util.*

class AdminViewModel(
    private val repository: AppointmentRepository = AppointmentRepository()
) : ViewModel() {

    private val _todayAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val todayAppointments: StateFlow<List<Appointment>> = _todayAppointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadTodayAppointments() {
        val today = formatter.format(Date())
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _todayAppointments.value = repository.getAppointments(today)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                repository.cancelAppointment(appointmentId)
                loadTodayAppointments()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
