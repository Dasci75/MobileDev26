package com.example.mobiledev.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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


class CityViewModel(private val countryName: String) : ViewModel() {

    private val _cityState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val cityState: StateFlow<CityUiState> = _cityState.asStateFlow()

    init {
        getCities()
    }

    private fun normalizeString(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "").lowercase()
    }

    private fun formatCityName(cityName: String): String {
        return cityName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    }

    fun getCities() {
        viewModelScope.launch {
            _cityState.value = CityUiState.Loading
            try {
                val db = Firebase.firestore
                val tripsResult = db.collection("trips").whereEqualTo("country", countryName).get().await()
                val citiesFromTrips = tripsResult.toObjects(Trip::class.java).mapNotNull { it.cityId }.map { formatCityName(it) }.distinct()

                val citiesResult = db.collection("countries").document(countryName).collection("cities").get().await()
                val citiesFromCities = citiesResult.map { formatCityName(it.id) }.distinct()

                val allCities = (citiesFromTrips + citiesFromCities).distinct().sorted()
                _cityState.value = CityUiState.Success(allCities)
            } catch (e: Exception) {
                Log.e("CityViewModel", "Error fetching cities for country $countryName", e)
                _cityState.value = CityUiState.Error
            }
        }
    }

    fun addCity(cityName: String) {
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val capitalizedCityName = cityName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val normalizedCityName = normalizeString(capitalizedCityName)

                val citiesQuery = db.collection("countries").document(countryName).collection("cities").get().await()
                val existingCities = citiesQuery.map { normalizeString(it.id) }
                if (existingCities.contains(normalizedCityName)) {
                    return@launch
                }


                
                db.collection("countries").document(countryName).collection("cities").document(capitalizedCityName).set(mapOf("name" to capitalizedCityName)).await()
                getCities() // Refresh the list
            } catch (e: Exception) {
                Log.e("CityViewModel", "Error adding city", e)
            }
        }
    }

    fun refresh() {
        getCities()
    }
}

class CityViewModelFactory(private val countryName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CityViewModel(countryName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
