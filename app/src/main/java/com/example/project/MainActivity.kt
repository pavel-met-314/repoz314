package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.theme.ProjectTheme
import com.google.firebase.FirebaseApp
import presentation.navigation.AppNavHost
import presentation.navigation.BottomNavBar
import presentation.navigation.Screen
import presentation.viewmodel.AuthViewModel
import presentation.viewmodel.SessionState

class MainActivity : ComponentActivity() {

    private val screensWithoutBottomBar = listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.AdminDashboard.route
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = FirebaseApp.getInstance()
        Log.d("FIREBASE", "Firebase initialized: ${app.name}")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectTheme {
                val authViewModel: AuthViewModel = viewModel()
                val sessionState by authViewModel.sessionState.collectAsState()

                when (sessionState) {
                    is SessionState.Checking -> {
                        // Показываем загрузку пока проверяем сессию
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> {
                        val startDestination = when (sessionState) {
                            is SessionState.AuthenticatedAdmin -> Screen.AdminDashboard.route
                            is SessionState.AuthenticatedClient -> Screen.Services.route
                            else -> Screen.Login.route
                        }

                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Scaffold(
                            bottomBar = {
                                if (currentRoute !in screensWithoutBottomBar) {
                                    BottomNavBar(navController = navController)
                                }
                            }
                        ) { innerPadding ->
                            AppNavHost(
                                navController = navController,
                                startDestination = startDestination,
                                innerPadding = innerPadding,
                                authViewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

