package com.example.mobiledev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.TripViewModel
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.example.mobiledev.ui.RatingBar // Import the common RatingBar
import androidx.compose.material.icons.filled.Star // Import Star icon
import com.google.ai.client.generativeai.Chat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel = viewModel(),
    paddingValues: PaddingValues // Added paddingValues parameter
) {
    val tripState by tripViewModel.tripState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), // Apply padding from parent Scaffold
        contentAlignment = Alignment.Center
    ) {
        when (val state = tripState) {
            is TripUiState.Loading -> CircularProgressIndicator()
            is TripUiState.Error -> Text("Error: Could not load trip details.")
            is TripUiState.Success -> {
                val trip = state.trips.find { it.id == tripId }
                if (trip == null) {
                    Text("Trip not found")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                            .verticalScroll(rememberScrollState())
                    ) {
                        TripDetailsContent(trip = trip)
                    }
                    FloatingActionButton(
                        onClick = { /* TODO: Implement join chat functionality */ },
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Align to bottom end of the Box
                            .padding(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, "Join Chat", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TripDetailsContent(trip: Trip) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Main Image (photo1)
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(trip.photoUrl?.get("photo1"))
                .crossfade(true)
                .build(),
            contentDescription = "${trip.name} main image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp) // Make image taller
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) // Rounded bottom corners
                .background(Color.Gray.copy(alpha = 0.5f)), // Placeholder background
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Small Images (photo2, photo3, photo4)
        val photoKeys = listOf("photo2", "photo3", "photo4")
        val photos = photoKeys.mapNotNull { key ->
            trip.photoUrl?.get(key)?.let { url -> url }
        }
        if (photos.isNotEmpty()) {
            Text(
                text = "More Photos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { photoUrl ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${trip.name} additional image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray.copy(alpha = 0.5f)), // Placeholder background
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Details & Description
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = "Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Locatie: Lat ${trip.latitude ?: "N/A"}, Lon ${trip.longitude ?: "N/A"}", fontSize = 14.sp)
                    Text(text = "Categorie: ${trip.category ?: ""}", fontSize = 14.sp)
                    trip.createdAt?.let {
                        Text(text = "Toegevoegd: ${dateFormat.format(it.toDate())}", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trip.description ?: "No description available.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Rating Bar
            Text(
                text = "Rating",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(16.dp)) // Extra space for FAB
        }
    }
}


