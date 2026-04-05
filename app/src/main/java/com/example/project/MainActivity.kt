package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.theme.ProjectTheme
import com.google.firebase.FirebaseApp
import presentation.navigation.AppNavHost
import presentation.navigation.BottomNavBar
import presentation.navigation.Screen

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
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
