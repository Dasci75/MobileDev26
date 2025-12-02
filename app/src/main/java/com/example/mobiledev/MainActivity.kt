package com.example.mobiledev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobiledev.ui.Screen
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.mobiledev.AddTripScreen
import com.example.mobiledev.ui.GeoViewModel
import com.example.mobiledev.ui.TripViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.example.mobiledev.ui.CityViewModel
import com.example.mobiledev.ui.CityViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileDevTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted: Boolean ->
                        if (isGranted) {
                            // Permission granted
                        } else {
                            // Permission denied
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val geoViewModel: GeoViewModel = viewModel()

                    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

                    DisposableEffect(auth) {
                        val authStateListener = FirebaseAuth.AuthStateListener {
                            isLoggedIn = it.currentUser != null
                        }
                        auth.addAuthStateListener(authStateListener)
                        onDispose {
                            auth.removeAuthStateListener(authStateListener)
                        }
                    }

                    val startDestination = if (isLoggedIn) "main" else "login"
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

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
                                CountrySelectionScreen(navController = navController, geoViewModel = geoViewModel, paddingValues = paddingValues)
                            }
                            navigation(
                                startDestination = "cityList",
                                route = "citySelection/{country}"
                            ) {
                                composable("cityList") { backStackEntry ->
                                    val parentEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry("citySelection/{country}")
                                    }
                                    val country = parentEntry.arguments?.getString("country")
                                    val cityViewModel: CityViewModel = viewModel(
                                        viewModelStoreOwner = parentEntry,
                                        factory = CityViewModelFactory(country!!)
                                    )
                                    CitySelectionScreen(
                                        navController = navController,
                                        countryName = country,
                                        cityViewModel = cityViewModel,
                                        paddingValues = paddingValues
                                    )
                                }
                                composable("addCity/{from}") { backStackEntry ->
                                     val parentEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry("citySelection/{country}")
                                    }
                                    val country = parentEntry.arguments?.getString("country")
                                    val from = backStackEntry.arguments?.getString("from")
                                    AddCityScreen(
                                        navController = navController,
                                        countryName = country,
                                        from = from
                                    )
                                }
                            }
                            composable("chat") { 
                                ChatListScreen(navController = navController, paddingValues = paddingValues)
                            }
                            composable("chat/{chatId}") { backStackEntry ->
                                val chatId = backStackEntry.arguments?.getString("chatId")
                                ChatScreen(chatId = chatId, paddingValues = paddingValues)
                            }
                            composable("dashboard") {
                                DashboardScreen(paddingValues = paddingValues)
                            }
                            composable("addTrip") {
                                val tripViewModel: TripViewModel = viewModel()
                                AddTripScreen(navController = navController, paddingValues = paddingValues, geoViewModel = geoViewModel, tripViewModel = tripViewModel)
                            }
                            composable("addCountry/{from}") { backStackEntry ->
                                val from = backStackEntry.arguments?.getString("from")
                                AddCountryScreen(navController = navController, from = from)
                            }
                            navigation(
                                startDestination = "addTripCountryList",
                                route = "addTripCountrySelection"
                            ) {
                                composable("addTripCountryList") {
                                    AddTripCountrySelectionScreen(navController = navController, geoViewModel = geoViewModel, paddingValues = paddingValues)
                                }
                            }
                            navigation(
                                startDestination = "addTripCityList",
                                route = "addTripCitySelection/{country}"
                            ) {
                                composable("addTripCityList") { backStackEntry ->
                                    val parentEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry("addTripCitySelection/{country}")
                                    }
                                    val country = parentEntry.arguments?.getString("country")
                                    val cityViewModel: CityViewModel = viewModel(
                                        viewModelStoreOwner = parentEntry,
                                        factory = CityViewModelFactory(country!!)
                                    )
                                    AddTripCitySelectionScreen(
                                        navController = navController,
                                        countryName = country,
                                        cityViewModel = cityViewModel,
                                        paddingValues = paddingValues
                                    )
                                }
                                                                composable("addCity/{from}") { backStackEntry ->
                                                                    val parentEntry = remember(backStackEntry) {
                                                                        navController.getBackStackEntry("addTripCitySelection/{country}")
                                                                    }
                                                                    val country = parentEntry.arguments?.getString("country")
                                                                    val from = backStackEntry.arguments?.getString("from")
                                                                    AddCityScreen(
                                                                        navController = navController,
                                                                        countryName = country,
                                                                        from = from
                                                                    )
                                                                }                            }
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