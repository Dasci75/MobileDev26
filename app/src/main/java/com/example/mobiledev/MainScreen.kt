package com.example.mobiledev

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.*
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.categories
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val TAG = "MainScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    tripViewModel: TripViewModel = viewModel(),
    geoViewModel: GeoViewModel,
    paddingValues: PaddingValues
) {
    val tripState by tripViewModel.tripState.collectAsState()
    val countryState by geoViewModel.countryState.collectAsState()
    val cityState by geoViewModel.cityState.collectAsState()

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    val isFilterApplied = selectedCountry != null || selectedCity != null || selectedCategory != null

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                tripViewModel.getTrips(selectedCountry, selectedCity, selectedCategory)
                isRefreshing = false
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                }
            }
        }
    }

    LaunchedEffect(selectedCountry) {
        selectedCountry?.let {
            geoViewModel.getCities(it)
            selectedCity = null // Reset city when country changes
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            TopBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onFilterClick = { showFilterDialog = true }
            )

            if (showFilterDialog) {
                FilterDialog(
                    countryState = countryState,
                    cityState = cityState,
                    categories = categories, // Use the predefined categories
                    selectedCountry = selectedCountry,
                    selectedCity = selectedCity,
                    selectedCategory = selectedCategory,
                    onCountrySelected = { selectedCountry = it },
                    onCitySelected = { selectedCity = it },
                    onCategorySelected = { selectedCategory = it },
                    onDismiss = { showFilterDialog = false },
                    onSearch = {
                        tripViewModel.getTrips(selectedCountry, selectedCity, selectedCategory)
                        showFilterDialog = false
                    },
                    onClear = {
                        selectedCountry = null
                        selectedCity = null
                        selectedCategory = null
                        tripViewModel.getTrips(null, null, null)
                        showFilterDialog = false
                    }
                )
            }

            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                when (val state = tripState) {
                    is TripUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TripUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error fetching trips")
                        }
                    }
                    is TripUiState.Success -> {
                        val filteredTrips = state.trips.filter {
                            it.name?.contains(searchText, ignoreCase = true) == true
                        }

                        if (filteredTrips.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No trips found.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (isFilterApplied) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp) // specify a height for the map
                                                .padding(16.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        ) {
                                            OsmMapView(trips = filteredTrips, userLocation = userLocation)
                                        }
                                    }
                                }
                                items(filteredTrips) { trip ->
                                    TripItem(trip = trip, navController = navController)
                                }
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        FloatingActionButton(
            onClick = { navController.navigate("addTrip") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Trip")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(searchText: String, onSearchTextChange: (String) -> Unit, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Search for a trip") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter Trips")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    countryState: CountryUiState,
    cityState: CityUiState,
    categories: List<String>,
    selectedCountry: String?,
    selectedCity: String?,
    selectedCategory: String?,
    onCountrySelected: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Country Dropdown
                FilterDropdown(
                    label = "Select Country",
                    items = (countryState as? CountryUiState.Success)?.countries ?: emptyList(),
                    selectedValue = selectedCountry,
                    onSelected = onCountrySelected
                )
                Spacer(modifier = Modifier.height(8.dp))

                // City Dropdown
                FilterDropdown(
                    label = "Select City",
                    items = (cityState as? CityUiState.Success)?.cities ?: emptyList(),
                    selectedValue = selectedCity,
                    onSelected = onCitySelected,
                    enabled = selectedCountry != null
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Category Dropdown
                FilterDropdown(
                    label = "Select Category",
                    items = categories,
                    selectedValue = selectedCategory,
                    onSelected = onCategorySelected
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onClear) {
                        Text("Clear")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSearch) {
                        Text("Search")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    items: List<String>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trip.photoUrl?.get("photo1"))
                    .crossfade(true)
                    .build(),
                contentDescription = "${trip.name} main image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Gray.copy(alpha = 0.5f)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = trip.name ?: "Unknown Trip",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trip.description ?: "No description available.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                RatingBar(rating = trip.rating ?: 0.0)
            }
        }
    }
}

@Composable
fun OsmMapView(trips: List<Trip>, userLocation: GeoPoint?) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val validTrips = trips.filter { it.latitude != null && it.longitude != null }
            if (validTrips.isNotEmpty()) {
                validTrips.forEach { trip ->
                    val geoPoint = GeoPoint(trip.latitude!!, trip.longitude!!)
                    val marker = Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = trip.name
                    }
                    mapView.overlays.add(marker)
                }
                val firstTripPoint = GeoPoint(validTrips[0].latitude!!, validTrips[0].longitude!!)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(firstTripPoint)
            } else if (userLocation != null) {
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(userLocation)
                val marker = Marker(mapView).apply {
                    position = userLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "My Location"
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MobileDevTheme {
        val context = LocalContext.current
        MainScreen(
            navController = rememberNavController(),
            paddingValues = PaddingValues(0.dp),
            geoViewModel = viewModel(factory = GeoViewModelFactory(context.applicationContext as Application))
        )
    }
}
