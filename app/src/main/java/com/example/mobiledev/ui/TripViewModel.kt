package com.example.mobiledev.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.Review
import com.example.mobiledev.data.Trip
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val categories = listOf(
    "Restaurant", "Bar", "Café", "Club", "Nachtleven", "Theater", "Bioscoop", "Museum",
    "Kunstgalerie", "Concertzaal", "Park", "Zoo", "Dierenpark", "Escape Room", "Bowling",
    "Arcade", "Pretpark", "Zwembad", "Wellness", "Strand", "Shopping", "Winkelcentrum",
    "Sportevenement", "Wandeling", "Hiking", "Festival", "Markt", "Bibliotheek",
    "Historische locatie", "Kasteel", "Monument", "Foodtruck", "Streetfood", "Karaoke bar",
    "Comedy club", "Wijnproeverij", "Brouwerij", "Workshop", "Cursus", "Casino"
)

sealed interface TripUiState {
    data class Success(val trips: List<Trip>) : TripUiState
    object Error : TripUiState
    object Loading : TripUiState
}

open class TripViewModel : ViewModel() {

    private val _tripState = MutableStateFlow<TripUiState>(TripUiState.Loading)
    open val tripState: StateFlow<TripUiState> = _tripState.asStateFlow()

    init {
        getTrips(null, null, null)
    }

    fun getTrips(country: String?, city: String?, category: String?) {
        viewModelScope.launch {
            _tripState.value = TripUiState.Loading
            try {
                val db = Firebase.firestore
                var query: Query = db.collection("trips")

                if (country != null) {
                    query = query.whereEqualTo("country", country)
                }
                if (city != null) {
                    query = query.whereEqualTo("cityId", city)
                }
                if (category != null) {
                    query = query.whereEqualTo("category", category)
                }

                val result = query.get().await()
                val trips = result.documents.map { document ->
                    document.toObject(Trip::class.java)?.copy(id = document.id)
                }.filterNotNull()
                _tripState.value = TripUiState.Success(trips)
            } catch (e: Exception) {
                Log.e("TripViewModel", "Error fetching trips", e)
                _tripState.value = TripUiState.Error
            }
        }
    }

    fun addReview(tripId: String, review: Review) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val tripRef = db.collection("trips").document(tripId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(tripRef)
                    val trip = snapshot.toObject(Trip::class.java)
                    if (trip != null) {
                        val newReviews = trip.reviews + review

                        val totalRating = newReviews.sumOf { it.rating }
                        val newAverageRating = if (newReviews.isNotEmpty()) {
                            totalRating / newReviews.size
                        } else {
                            0.0
                        }

                        transaction.update(
                            tripRef,
                            mapOf(
                                "reviews" to newReviews,
                                "rating" to newAverageRating
                            )
                        )
                    }
                }.await()

                refresh()

            } catch (e: Exception) {
                Log.e("TripViewModel", "Error adding review", e)
            }
        }
    }

    fun refresh() {
        getTrips(null, null, null)
    }
}
