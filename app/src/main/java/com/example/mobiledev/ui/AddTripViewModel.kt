package com.example.mobiledev.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.Trip
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class AddTripViewModel : ViewModel() {
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var category by mutableStateOf("")
    var country by mutableStateOf<String?>(null)
    var city by mutableStateOf<String?>(null)
    var photoUris by mutableStateOf<List<String>>(emptyList())
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)

    fun isFormValid(): Boolean {
        return name.isNotBlank() &&
                description.isNotBlank() &&
                category.isNotBlank() &&
                country != null &&
                city != null &&
                photoUris.isNotEmpty() &&
                latitude != null &&
                longitude != null
    }

    fun saveTrip(onTripSaved: () -> Unit) {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            if (user != null) {
                val imageUrls = uploadImages(photoUris)
                val capitalizedCountry = country?.split(" ")?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val capitalizedCity = city?.split(" ")?.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val trip = Trip(
                    id = UUID.randomUUID().toString(),
                    userId = user.uid,
                    name = name,
                    description = description,
                    category = category,
                    country = capitalizedCountry,
                    cityId = capitalizedCity,
                    createdAt = Timestamp(Date()),
                    latitude = latitude,
                    longitude = longitude,
                    photoUrl = imageUrls.entries.associate { (key, value) -> "photo$key" to value }
                )
                saveTripToFirestore(trip, onTripSaved)
            }
        }
    }

    private suspend fun uploadImages(uris: List<String>): Map<Int, String> {
        val storageRef = Firebase.storage.reference
        val imageUrls = mutableMapOf<Int, String>()
        uris.forEachIndexed { index, uriString ->
            val uri = Uri.parse(uriString)
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
            try {
                imageRef.putFile(uri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                imageUrls[index + 1] = downloadUrl
            } catch (e: Exception) {
                // Handle exception
            }
        }
        return imageUrls
    }

    private fun saveTripToFirestore(trip: Trip, onTripSaved: () -> Unit) {
        val db = Firebase.firestore
        db.collection("trips").document(trip.id).set(trip)
            .addOnSuccessListener {
                onTripSaved()
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}
