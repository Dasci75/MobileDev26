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
                    val startDestination = if (auth.currentUser != null) "main" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true } // Prevents going back to login
                                }
                            })
                        }
                        composable(
                            "main?city={city}",
                            arguments = listOf(navArgument("city") { nullable = true })
                        ) { backStackEntry ->
                            val city = backStackEntry.arguments?.getString("city")
                            MainScreen(navController = navController, city = city)
                        }
                        composable("tripDetails/{tripId}") { backStackEntry ->
                            val tripId = backStackEntry.arguments?.getString("tripId")
                            TripDetailsScreen(tripId = tripId, navController = navController)
                        }
                        composable("settings") {
                            SettingsScreen(navController = navController, auth = auth)
                        }
                        composable("countrySelection") {
                            CountrySelectionScreen(navController = navController)
                        }
                        composable("citySelection/{country}") { backStackEntry ->
                            val country = backStackEntry.arguments?.getString("country")
                            CitySelectionScreen(navController = navController, country = country)
                        }
                    }
                }
            }
        }
    }
}
