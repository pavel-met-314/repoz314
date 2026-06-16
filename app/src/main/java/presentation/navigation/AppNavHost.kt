package presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import domain.model.Service
import presentation.screens.AdminDashboardScreen
import presentation.screens.BookingScreen
import presentation.screens.LoginScreen
import presentation.screens.MyAppointmentsScreen
import presentation.screens.PortfolioScreen
import presentation.screens.ProfileScreen
import presentation.screens.RegisterScreen
import presentation.screens.ServicesScreen
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.PortfolioViewModel
import presentation.viewmodel.SessionState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Services : Screen("services")
    object Booking : Screen("booking")
    object MyAppointments : Screen("my_appointments")
    object Profile : Screen("profile")
    object AdminDashboard : Screen("admin_dashboard")
    object Portfolio : Screen("portfolio")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    innerPadding: PaddingValues = PaddingValues(),
    authViewModel: AuthViewModel
) {
    // Выбранная услуга передаётся через общий стейт между Services и Booking
    var pendingService by remember { mutableStateOf<Service?>(null) }

    // Общий ViewModel для портфолио (чтобы не перезагружать при переходах)
    val portfolioViewModel: PortfolioViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val sessionState by authViewModel.sessionState.collectAsState()
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Unauthenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null &&
                currentRoute != Screen.Login.route &&
                currentRoute != Screen.Register.route
            ) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { isAdmin ->
                    if (isAdmin) {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Services.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                authViewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Services.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(Screen.Services.route) {
            ServicesScreen(
                onServiceSelected = { service ->
                    pendingService = service
                    navController.navigate(Screen.Booking.route)
                }
            )
        }
        composable(Screen.Booking.route) {
            val service = pendingService
            if (service != null) {
                BookingScreen(
                    service = service,
                    onBack = { navController.popBackStack() },
                    onBookingSuccess = {
                        navController.navigate(Screen.Services.route) {
                            popUpTo(Screen.Services.route) { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            } else {
                // Если сервис не выбран — возвращаемся назад
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }
        composable(Screen.MyAppointments.route) { MyAppointmentsScreen(authViewModel = authViewModel) }
        composable(Screen.Profile.route) { ProfileScreen(authViewModel = authViewModel) }
        composable(Screen.Portfolio.route) { PortfolioScreen(portfolioViewModel = portfolioViewModel) }
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(authViewModel = authViewModel) }
    }
}
