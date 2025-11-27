package com.example.mobiledev

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.TripViewModel
import com.example.mobiledev.ui.theme.MobileDevTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale // Ensure ContentScale is imported

private const val TAG = "MainScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    city: String? = null,
    tripViewModel: TripViewModel = viewModel()
) {
    val tripState by tripViewModel.tripState.collectAsState()

    // Use LaunchedEffect to trigger data refresh when MainScreen is active
    val currentOnGetTrips by rememberUpdatedState(tripViewModel::getTrips)
    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "MainScreen LaunchedEffect: Calling getTrips()")
        currentOnGetTrips()
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController = navController, tripViewModel = tripViewModel) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Handle add trip */ },
                shape = CircleShape,
                containerColor = Color(0xFFF9A825) // Orange
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Light gray background
        ) {
            SearchBar(navController = navController)
            when (val state = tripState) {
                is TripUiState.Loading -> {
                    Log.d(TAG, "TripUiState: Loading")
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TripUiState.Error -> {
                    Log.d(TAG, "TripUiState: Error")
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error fetching trips")
                    }
                }
                is TripUiState.Success -> {
                    Log.d(TAG, "TripUiState: Success with ${state.trips.size} trips")
                    val tripsToShow = if (city != null) {
                        state.trips.filter { it.cityId.equals(city, ignoreCase = true) }
                    } else {
                        state.trips
                    }
                    TripList(trips = tripsToShow, navController = navController)
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9A825)) // Orange
            .padding(16.dp)
    ) {
        Text(
            text = "CityTrip",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Home",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { navController.navigate("countrySelection") },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search for a city") },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            enabled = false
        )
    }
}

@Composable
fun TripList(trips: List<Trip>, navController: NavController) {
    if (trips.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No trips found for this location.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trips) { trip ->
                TripItem(trip = trip, navController = navController)
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("tripDetails/${trip.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Changed to surface for better contrast
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(trip.photoUrl?.get("photo1"))
                    .crossfade(true)
                    .build(),
                contentDescription = "${trip.name} main image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Make image taller
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Rounded top corners
                    .background(Color.Gray.copy(alpha = 0.5f)), // Placeholder background
                contentScale = androidx.compose.ui.layout.ContentScale.Crop // Crop to fill bounds
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = trip.name ?: "Unknown Trip",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp // Larger font for name
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trip.description ?: "No description available.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // Limit description to 2 lines
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                RatingBar(rating = trip.rating ?: 0.0)
            }
        }
    }
}

@Composable
fun RatingBar(rating: Double) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = if (i <= rating) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, tripViewModel: TripViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomAppBar(
        containerColor = Color(0xFFF9A825) // Orange
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                navController.navigate("main")
            }) {
                val isMainScreen = currentRoute?.startsWith("main") == true
                Text("Home", color = Color.White, fontWeight = if (isMainScreen) FontWeight.Bold else FontWeight.Normal)
            }
            TextButton(onClick = { /* TODO: Implement Dashboard */ }) {
                Text("Dashboard", color = Color.White, fontWeight = if (currentRoute == "dashboard") FontWeight.Bold else FontWeight.Normal)
            }
            TextButton(onClick = { navController.navigate("settings") }) {
                Text("Settings", color = Color.White, fontWeight = if (currentRoute == "settings") FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MobileDevTheme {
        MainScreen(rememberNavController(), city = null)
    }
}