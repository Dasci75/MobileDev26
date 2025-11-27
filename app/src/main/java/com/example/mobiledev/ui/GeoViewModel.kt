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

sealed interface CountryUiState {
    data class Success(val countries: List<String>) : CountryUiState
    object Error : CountryUiState
    object Loading : CountryUiState
}

class GeoViewModel : ViewModel() {

    private val _countryState = MutableStateFlow<CountryUiState>(CountryUiState.Loading)
    val countryState: StateFlow<CountryUiState> = _countryState.asStateFlow()

    init {
        getCountries()
    }

    fun getCountries() {
        viewModelScope.launch {
            _countryState.value = CountryUiState.Loading
            try {
                val db = Firebase.firestore
                val result = db.collection("trips").get().await()
                val countries = result.toObjects(Trip::class.java).mapNotNull { it.country }.distinct()
                _countryState.value = CountryUiState.Success(countries)
            } catch (e: Exception) {
                Log.e("GeoViewModel", "Error fetching countries", e)
                _countryState.value = CountryUiState.Error
            }
        }
    }

    fun refresh() {
        getCountries()
    }
}