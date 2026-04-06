package presentation.screens

import androidx.compose.runtime.Composable
import domain.model.Service
import presentation.ui.ServiceListScreen

@Composable
fun ServicesScreen(onServiceSelected: (Service) -> Unit = {}) {
    ServiceListScreen(onServiceSelected = onServiceSelected)
}
