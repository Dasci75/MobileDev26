package com.example.mobiledev

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mobiledev.ui.AddTripViewModel
import com.example.mobiledev.ui.GeoViewModel
import com.example.mobiledev.ui.TripViewModel
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.categories
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    addTripViewModel: AddTripViewModel = viewModel(),
    geoViewModel: GeoViewModel,
    tripViewModel: TripViewModel
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        addTripViewModel.photoUris = uris.map { it.toString() }.take(4)
    }
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    LaunchedEffect(key1 = navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selectedCountry")
            ?.observe(navController.currentBackStackEntry!!) {
                addTripViewModel.country = it
            }
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("selectedCity")
            ?.observe(navController.currentBackStackEntry!!) {
                addTripViewModel.city = it
            }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    addTripViewModel.latitude = location.latitude
                    addTripViewModel.longitude = location.longitude
                }
            }
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Success") },
                text = { Text("Trip saved successfully!") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                            geoViewModel.refreshCountries()
                            tripViewModel.getTrips(null, null, null)
                            navController.navigate("main") {
                                popUpTo("addTrip") { inclusive = true }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = addTripViewModel.name,
                    onValueChange = { addTripViewModel.name = it },
                    label = { Text("Trip Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = addTripViewModel.description,
                    onValueChange = { addTripViewModel.description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = addTripViewModel.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    addTripViewModel.category = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = addTripViewModel.country ?: "Select a country")
                    Button(onClick = { navController.navigate("addTripCountrySelection") }) {
                        Text("Select")
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = addTripViewModel.city ?: "Select a city")
                    Button(
                        onClick = {
                            val selectedCountry = addTripViewModel.country
                            if (selectedCountry != null) {
                                navController.navigate("addTripCitySelection/$selectedCountry")
                            }
                        },
                        enabled = addTripViewModel.country != null
                    ) {
                        Text("Select")
                    }
                }
            }
            item {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Photos (Max 4)")
                }
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(addTripViewModel.photoUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(100.dp)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(text = "Lat: ${"%.4f".format(addTripViewModel.latitude ?: 0.0)}")
                        Text(text = "Lon: ${"%.4f".format(addTripViewModel.longitude ?: 0.0)}")
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    OsmMapView(
                        latitude = addTripViewModel.latitude,
                        longitude = addTripViewModel.longitude,
                        onMapClick = {
                            addTripViewModel.latitude = it.latitude
                            addTripViewModel.longitude = it.longitude
                        }
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        isLoading = true
                        addTripViewModel.saveTrip {
                            isLoading = false
                            showDialog = true
                        }
                    },
                    enabled = addTripViewModel.isFormValid(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Trip")
                }
            }
        }
    }
}

@Composable
fun OsmMapView(
    latitude: Double?,
    longitude: Double?,
    onMapClick: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let(onMapClick)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        return false
                    }
                }
                overlays.add(MapEventsOverlay(eventsReceiver))
            }
        },
        update = { mapView ->
            mapView.overlays.removeIf { it is Marker }
            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)
                val marker = Marker(mapView)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                mapView.controller.setCenter(geoPoint)
            }
            mapView.invalidate()
        }
    )
}