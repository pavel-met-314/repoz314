package presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Appointment
import domain.model.Block
import domain.model.ClientHistory
import domain.model.Schedule
import domain.model.Service
import domain.model.VisitNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.AppointmentRepository
import presentation.repository.ServiceRepository
import java.text.SimpleDateFormat
import java.util.*

class AdminViewModel(
    private val repository: AppointmentRepository = AppointmentRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─── Записи на сегодня ────────────────────────────────────────────────────
    private val _todayAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val todayAppointments: StateFlow<List<Appointment>> = _todayAppointments

    // ─── Записи на выбранную дату ─────────────────────────────────────────────
    private val _selectedDateAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val selectedDateAppointments: StateFlow<List<Appointment>> = _selectedDateAppointments

    // ─── Все записи ───────────────────────────────────────────────────────────
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val allAppointments: StateFlow<List<Appointment>> = _allAppointments

    // ─── Расписание ───────────────────────────────────────────────────────────
    private val _schedule = MutableStateFlow<Schedule?>(null)
    val schedule: StateFlow<Schedule?> = _schedule

    // ─── Личные блоки ─────────────────────────────────────────────────────────
    private val _blocks = MutableStateFlow<List<Block>>(emptyList())
    val blocks: StateFlow<List<Block>> = _blocks

    // ─── Услуги ───────────────────────────────────────────────────────────────
    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    // ─── История клиентов ─────────────────────────────────────────────────────
    private val _clientHistories = MutableStateFlow<List<ClientHistory>>(emptyList())
    val clientHistories: StateFlow<List<ClientHistory>> = _clientHistories

    // ─── Общий UI-стейт ───────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // ─── Загрузка записей на сегодня ──────────────────────────────────────────

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

    // ─── Загрузка записей на конкретную дату ──────────────────────────────────

    fun loadAppointmentsForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedDateAppointments.value = repository.getAppointments(date)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─── Отмена записи ────────────────────────────────────────────────────────

    fun cancelAppointment(appointmentId: String, onRefreshDate: String? = null) {
        viewModelScope.launch {
            try {
                repository.cancelAppointment(appointmentId)
                if (onRefreshDate != null) loadAppointmentsForDate(onRefreshDate)
                else loadTodayAppointments()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── Расписание ───────────────────────────────────────────────────────────

    fun loadSchedule(date: String) {
        viewModelScope.launch {
            try {
                _schedule.value = repository.getSchedule(date)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                repository.saveSchedule(schedule)
                _schedule.value = schedule
                _successMessage.value = "Расписание сохранено"
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── Личные блоки ─────────────────────────────────────────────────────────

    fun loadBlocks(date: String) {
        viewModelScope.launch {
            try {
                _blocks.value = repository.getBlocks(date)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveBlock(block: Block) {
        viewModelScope.launch {
            try {
                repository.saveBlock(block)
                _successMessage.value = "Блок добавлен"
                loadBlocks(block.date)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteBlock(blockId: String, date: String) {
        viewModelScope.launch {
            try {
                repository.deleteBlock(blockId)
                loadBlocks(date)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── Услуги ───────────────────────────────────────────────────────────────

    fun loadServices() {
        viewModelScope.launch {
            try {
                _services.value = serviceRepository.getServices()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun saveService(service: Service) {
        viewModelScope.launch {
            try {
                repository.saveService(service)
                _successMessage.value = "Услуга сохранена"
                loadServices()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteService(serviceId)
                loadServices()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── История клиентов ─────────────────────────────────────────────────────

    fun loadClientHistories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _clientHistories.value = repository.getAllClientHistories()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveClientNote(clientId: String, note: String) {
        viewModelScope.launch {
            try {
                val history = _clientHistories.value.find { it.clientId == clientId } ?: return@launch
                val updatedNotes = history.notes.toMutableList().also { list ->
                    val last = list.lastOrNull()
                    if (last != null) {
                        list[list.lastIndex] = last.copy(note = note)
                    }
                }
                val updated = history.copy(notes = updatedNotes)
                repository.saveClientHistory(updated)
                _successMessage.value = "Заметка сохранена"
                loadClientHistories()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
