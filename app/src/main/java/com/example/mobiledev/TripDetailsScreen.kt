package com.example.mobiledev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.ui.theme.MobileDevTheme
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.compose.AsyncImage
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.TripViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TripDetailsScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel = viewModel()
) {
    val tripState by tripViewModel.tripState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = tripState) {
            is TripUiState.Loading -> CircularProgressIndicator()
            is TripUiState.Error -> Text("Error: Could not load trip details.")
            is TripUiState.Success -> {
                val trip = state.trips.find { it.id == tripId }
                if (trip == null) {
                    Text("Trip not found")
                } else {
                    Scaffold(
                        topBar = { DetailTopBar(trip = trip) },
                        bottomBar = { BottomNavigationBar(navController = navController, tripViewModel = tripViewModel) }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5))
                                .verticalScroll(rememberScrollState())
                        ) {
                            TripDetailsContent(trip = trip)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailTopBar(trip: Trip) {
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
            text = trip.name ?: "",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TripDetailsContent(trip: Trip) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Main Image (photo1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            val mainImageUrl = trip.photoUrl?.get("photo1")
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(mainImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "${trip.name} main image",
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray) // Placeholder background
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small Images (photo2, photo3, photo4)
            val photoKeys = listOf("photo2", "photo3", "photo4")
            val photos = photoKeys.mapNotNull { key ->
                trip.photoUrl?.get(key)?.let { url -> key to url }
            }

            if (photos.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photos.forEach { (_, photoUrl) ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${trip.name} additional image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray) // Placeholder background
                        )
                    }
                }
            }
            
            // Location and Category Box
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Locatie: Lat ${trip.latitude ?: "N/A"}, Lon ${trip.longitude ?: "N/A"}", fontSize = 10.sp)
                    Text(text = "Categorie: ${trip.category ?: ""}", fontSize = 10.sp)
                    trip.createdAt?.let {
                        Text(text = "Toegevoegd: ${dateFormat.format(it.toDate())}", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = trip.description ?: "",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Rating Bar
        RatingBar(rating = trip.rating ?: 0.0)
        Spacer(modifier = Modifier.height(16.dp))

        // Review Button
        Button(
            onClick = { /* TODO: Handle Review action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825))
        ) {
            Text(text = "Review", color = Color.White, fontSize = 18.sp)
        }
    }
}