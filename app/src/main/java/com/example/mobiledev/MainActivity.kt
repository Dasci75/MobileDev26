package com.example.mobiledev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mobiledev.ChatScreen
import com.example.mobiledev.DashboardScreen

import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.mobiledev.ui.Screen
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileDevTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                    DisposableEffect(auth) {
                        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                            isLoggedIn = firebaseAuth.currentUser != null
                        }
                        auth.addAuthStateListener(authStateListener)
                        onDispose {
                            auth.removeAuthStateListener(authStateListener)
                        }
                    }

                    val startDestination = if (isLoggedIn) "main" else "login"

                    Scaffold(
                        topBar = {
                            TopAppBar(title = { Text("CityTrip") })
                        },
                        bottomBar = {
                            if (isLoggedIn) { // Only show bottom bar if logged in
                                BottomNavigationBar(navController = navController)
                            }
                        }
                    ) { paddingValues ->
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("login") {
                                LoginScreen(onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true } // Prevents going back to login
                                    }
                                }, paddingValues = paddingValues)
                            }
                            composable(
                                "main?city={city}",
                                arguments = listOf(navArgument("city") { nullable = true })
                            ) { backStackEntry ->
                                val city = backStackEntry.arguments?.getString("city")
                                MainScreen(navController = navController, city = city, paddingValues = paddingValues)
                            }
                            composable("tripDetails/{tripId}") { backStackEntry ->
                                val tripId = backStackEntry.arguments?.getString("tripId")
                                TripDetailsScreen(tripId = tripId, navController = navController, paddingValues = paddingValues)
                            }
                            composable("settings") {
                                SettingsScreen(navController = navController, auth = auth, paddingValues = paddingValues)
                            }
                            composable("countrySelection") {
                                CountrySelectionScreen(navController = navController, paddingValues = paddingValues)
                            }
                            composable("citySelection/{country}") { backStackEntry ->
                                val country = backStackEntry.arguments?.getString("country")
                                CitySelectionScreen(navController = navController, countryName = country, paddingValues = paddingValues)
                            }
                            composable("chat") {
                                ChatScreen(paddingValues = paddingValues)
                            }
                            composable("dashboard") {
                                DashboardScreen(paddingValues = paddingValues)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Chat,
        Screen.Settings
    )

    NavigationBar(
        containerColor = Color(0xFFF9A825) // Orange
    ) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute?.startsWith(screen.route) == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                            inclusive = false // Ensure the start destination itself is not popped
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color(0xFFF9A825) // Orange
                )
            )
        }
    }
}