package presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import presentation.screens.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Services : Screen("services")
    object Booking : Screen("booking")
    object MyAppointments : Screen("my_appointments")
    object Profile : Screen("profile")
    object AdminDashboard : Screen("admin_dashboard")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    innerPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { isAdmin ->
                    if (isAdmin) navController.navigate(Screen.AdminDashboard.route)
                    else navController.navigate(Screen.Services.route)
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Services.route) },
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Services.route) { ServicesScreen() }
        composable(Screen.Booking.route) { BookingScreen() }
        composable(Screen.MyAppointments.route) { MyAppointmentsScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen() }
    }
}
