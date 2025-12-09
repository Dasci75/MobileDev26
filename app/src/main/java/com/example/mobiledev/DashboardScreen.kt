package com.example.mobiledev

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.TripViewModel
import com.example.mobiledev.ui.categories
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "DashboardScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, // Toegevoegd voor navigatie
    paddingValues: PaddingValues,
    tripViewModel: TripViewModel = viewModel()
) {
    Log.d(TAG, "DashboardScreen is composing")
    val tripUiState by tripViewModel.tripState.collectAsState()

    // 1. Haal de huidige ingelogde gebruiker ID op
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Filter Dropdown ---
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = selectedCategory ?: "All Categories",
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Categories") },
                    onClick = {
                        selectedCategory = null
                        tripViewModel.getTrips(null, null, null)
                        expanded = false
                    }
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            tripViewModel.getTrips(null, null, category)
                            expanded = false
                        }
                    )
                }
            }
        }

        // --- Lijst met Trips ---
        when (tripUiState) {
            is TripUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TripUiState.Success -> {
                val allTrips = (tripUiState as TripUiState.Success).trips

                // 2. Filter: Alleen trips van de huidige gebruiker
                val myTrips = if (currentUserId != null) {
                    allTrips.filter { trip -> trip.userId == currentUserId }
                } else {
                    emptyList()
                }

                if (myTrips.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentUserId == null) "Log in om trips te zien" else "Je hebt nog geen trips aangemaakt.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp), // Ruimte tussen kaarten
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(myTrips) { trip ->
                            // Hier gebruiken we de nieuwe Card layout
                            MyTripCard(
                                trip = trip,
                                onClick = {
                                    // Pas de route aan als die anders is in jouw app
                                    navController.navigate("tripDetails/${trip.id}")
                                }
                            )
                        }
                    }
                }
            }
            is TripUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading trips.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// --- De Nieuwe Card Component ---
@Composable
fun MyTripCard(
    trip: Trip,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // Iets hoger voor mooie indeling
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Afbeelding Links
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(trip.photoUrl?.get("photo1")) // Haal eerste foto
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = "Trip Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray)
            )

            // Tekst Rechts
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = trip.name ?: "Naamloos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Categorie Label
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = trip.category ?: "Geen categorie",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Locatie onderin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.cityId ?: trip.country ?: "Onbekende locatie",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}