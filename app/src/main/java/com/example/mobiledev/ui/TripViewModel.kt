package com.example.mobiledev.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.Trip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface TripUiState {
    data class Success(val trips: List<Trip>) : TripUiState
    object Error : TripUiState
    object Loading : TripUiState
}

open class TripViewModel : ViewModel() {

    private val _tripState = MutableStateFlow<TripUiState>(TripUiState.Loading)
    open val tripState: StateFlow<TripUiState> = _tripState.asStateFlow()

    init {
        getTrips()
    }

    fun getTrips() {
        viewModelScope.launch {
            _tripState.value = TripUiState.Loading
            try {
                val db = Firebase.firestore
                val result = db.collection("trips").get().await()
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

    fun refresh() {
        getTrips()
    }
}
