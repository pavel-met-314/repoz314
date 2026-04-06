package presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.model.Portfolio
import domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import presentation.repository.PortfolioRepository
import presentation.repository.ServiceRepository

sealed class PortfolioUiState {
    object Idle : PortfolioUiState()
    object Loading : PortfolioUiState()
    object Success : PortfolioUiState()
    data class Error(val message: String) : PortfolioUiState()
}

class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository = PortfolioRepository(),
    private val serviceRepository: ServiceRepository = ServiceRepository()
) : ViewModel() {

    private val _portfolio = MutableStateFlow<List<Portfolio>>(emptyList())
    val portfolio: StateFlow<List<Portfolio>> = _portfolio

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uploadState = MutableStateFlow<PortfolioUiState>(PortfolioUiState.Idle)
    val uploadState: StateFlow<PortfolioUiState> = _uploadState

    fun loadPortfolio() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _portfolio.value = portfolioRepository.getPortfolio()
            } catch (e: Exception) {
                // Показываем пустой список при ошибке
                _portfolio.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadServices() {
        viewModelScope.launch {
            try {
                _services.value = serviceRepository.getServices()
            } catch (_: Exception) {}
        }
    }

    fun uploadPhoto(uri: Uri, serviceId: String?, caption: String) {
        viewModelScope.launch {
            _uploadState.value = PortfolioUiState.Loading
            try {
                portfolioRepository.uploadPhoto(uri, serviceId, caption)
                _uploadState.value = PortfolioUiState.Success
                loadPortfolio()
            } catch (e: Exception) {
                _uploadState.value = PortfolioUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun deletePhoto(portfolio: Portfolio) {
        viewModelScope.launch {
            try {
                portfolioRepository.deletePhoto(portfolio)
                loadPortfolio()
            } catch (_: Exception) {}
        }
    }

    fun resetUploadState() {
        _uploadState.value = PortfolioUiState.Idle
    }
}

