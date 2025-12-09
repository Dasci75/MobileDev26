package com.example.mobiledev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobiledev.data.Review
import com.example.mobiledev.data.Trip
import com.example.mobiledev.ui.ChatViewModel
import com.example.mobiledev.ui.RatingBar
import com.example.mobiledev.ui.TripUiState
import com.example.mobiledev.ui.TripViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: String?,
    navController: NavController,
    tripViewModel: TripViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val tripState by tripViewModel.tripState.collectAsState()
    val auth = Firebase.auth
    val currentUserId = auth.currentUser?.uid

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
                        TripDetailsContent(trip = trip, tripViewModel = tripViewModel)
                    }

                    // Only show the FAB if the current user is logged in and is NOT the trip owner
                    if (currentUserId != null && trip.userId != null && currentUserId != trip.userId) {
                        FloatingActionButton(
                            onClick = {
                                chatViewModel.findOrCreateChat(trip.userId, trip.id) { chatId ->
                                    navController.navigate("chat/$chatId")
                                }
                            },
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
}

@Composable
fun TripDetailsContent(trip: Trip, tripViewModel: TripViewModel) {
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
                    Text(text = "Locatie: ${trip.cityId ?: "N/A"}, ${trip.country ?: "N/A"}", fontSize = 14.sp)
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

            // Reviews Section
            ReviewsSection(trip = trip, tripViewModel = tripViewModel)

            Spacer(modifier = Modifier.height(16.dp)) // Extra space for FAB
        }
    }
}

@Composable
fun ReviewsSection(trip: Trip, tripViewModel: TripViewModel) {
    Column {
        Text(
            text = "Reviews",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // List of reviews
        trip.reviews.forEach { review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    RatingBar(rating = review.rating)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "- ${review.userEmail}", // replace with user name later
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add a review
        AddReview(tripId = trip.id, tripViewModel = tripViewModel)
    }
}

@Composable
fun AddReview(tripId: String, tripViewModel: TripViewModel) {
    var rating by remember { mutableStateOf(0.0) }
    var comment by remember { mutableStateOf("") }
    val auth = Firebase.auth

    Column {
        Text(
            text = "Laat een review achter",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        RatingBar(rating = rating, onRatingChanged = { rating = it })
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Opmerking") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val userId = auth.currentUser?.uid
                val userEmail = auth.currentUser?.email ?: "Onbekend"
                if (userId != null) {
                    val review = Review(
                        userId = userId,
                        userEmail = userEmail,
                        rating = rating,
                        comment = comment,
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                    tripViewModel.addReview(tripId, review)
                    // Optionally clear fields after submitting
                    rating = 0.0
                    comment = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = auth.currentUser != null
        ) {
            Text("Verstuur")
        }
    }
}
