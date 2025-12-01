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
import java.text.Normalizer


class GeoViewModel : ViewModel() {

    private val _countryState = MutableStateFlow<CountryUiState>(CountryUiState.Loading)
    val countryState: StateFlow<CountryUiState> = _countryState.asStateFlow()

    init {
        getCountries()
    }

    private fun normalizeString(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "").lowercase()
    }

    fun getCountries() {
        viewModelScope.launch {
            _countryState.value = CountryUiState.Loading
            try {
                val db = Firebase.firestore
                val tripsResult = db.collection("trips").get().await()
                val countriesFromTrips = tripsResult.toObjects(Trip::class.java).mapNotNull { it.country }.distinct()

                val countriesResult = db.collection("countries").get().await()
                val countriesFromCountries = countriesResult.map { it.id }.distinct()

                val allCountries = (countriesFromTrips + countriesFromCountries).distinct().sorted()
                _countryState.value = CountryUiState.Success(allCountries)
            } catch (e: Exception) {
                Log.e("GeoViewModel", "Error fetching countries", e)
                _countryState.value = CountryUiState.Error
            }
        }
    }

    fun addCountry(countryName: String) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val capitalizedCountryName = countryName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val normalizedCountryName = normalizeString(capitalizedCountryName)

                val countriesQuery = db.collection("countries").get().await()
                val existingCountries = countriesQuery.map { normalizeString(it.id) }
                if (existingCountries.contains(normalizedCountryName)) {
                    return@launch
                }


                
                db.collection("countries").document(capitalizedCountryName).set(mapOf("name" to capitalizedCountryName)).await()
                getCountries() // Refresh the list
            } catch (e: Exception) {
                Log.e("GeoViewModel", "Error adding country", e)
            }
        }
    }

    fun refresh() {
        getCountries()
    }
}