package com.example.mobiledev

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.ui.theme.MobileDevTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class Trip(
    val id: Int,
    val title: String,
    val location: String,
    val rating: Float,
    val latitude: Double,
    val longitude: Double
)

// Dummy data for the list
val dummyTrips = listOf(
    Trip(1, "Eiffel tower", "Paris, France", 4.5f, 48.8584, 2.2945),
    Trip(2, "Pisa tower", "Pisa, Italy", 4.0f, 43.7230, 10.3966)
)

// MainScreen now accepts an optional city parameter to filter trips
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, city: String? = null) {
    // Filter the trips based on the selected city. If no city is provided, show all trips.
    val tripsToShow = if (city != null) {
        dummyTrips.filter { it.location.contains(city, ignoreCase = true) }
    } else {
        dummyTrips
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController = navController) },
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

            if (city != null && tripsToShow.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    OsmMapView(trips = tripsToShow)
                }
            }
            TripList(trips = tripsToShow, navController = navController)


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
            placeholder = { Text("Search") },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            enabled = false // Disable the text field to make the whole card clickable
        )
    }
}

@Composable
fun TripList(trips: List<Trip>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 300.dp), // Set a max height
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(trips) { trip ->
            TripItem(trip = trip, navController = navController)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9A825)) // Orange
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = trip.title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
                Text(text = trip.location, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                RatingBar(rating = trip.rating)
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Placeholder for the image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // In a real app, you would load an image here.
            }
        }
    }
}

@Composable
fun RatingBar(rating: Float) {
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

// The BottomNavigationBar now handles navigation correctly
@Composable
fun BottomNavigationBar(navController: NavController) {
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
            // "Home" button now navigates to the main screen and clears any filters
            TextButton(onClick = {
                navController.navigate("main") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) {
                // Highlight the button if on the main screen
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

@Composable
fun OsmMapView(trips: List<Trip>) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            // Initialize osmdroid configuration
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true) // Enable zoom
                controller.setZoom(12.0) // Default zoom level
            }
        },
        update = { mapView ->
            // Clear existing markers
            mapView.overlays.clear()

            // Add a marker for each trip
            if (trips.isNotEmpty()) {
                trips.forEach { trip ->
                    val geoPoint = GeoPoint(trip.latitude, trip.longitude)
                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = trip.title
                    mapView.overlays.add(marker)
                }

                // Center the map on the first trip in the list
                val firstTripPoint = GeoPoint(trips[0].latitude, trips[0].longitude)
                mapView.controller.setCenter(firstTripPoint)
            }

            // Redraw the map
            mapView.invalidate()
        }
    )
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MobileDevTheme {
        // Pass city as null in the preview
        MainScreen(rememberNavController(), city = null)
    }
}
