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

    private val _cityState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val cityState: StateFlow<CityUiState> = _cityState.asStateFlow()

    init {
        getCountries()
    }

    private fun formatCityName(cityName: String): String {
        return cityName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    }

    private fun getCountries() {
        viewModelScope.launch {
            _countryState.value = CountryUiState.Loading
            try {
                val db = Firebase.firestore
                val result = db.collection("trips").get().await()
                val countries = result.documents.mapNotNull { it.getString("country") }.distinct().sorted()
                _countryState.value = CountryUiState.Success(countries)
            } catch (e: Exception) {
                _countryState.value = CountryUiState.Error
            }
        }
    }

    fun getCities(countryName: String) {
        viewModelScope.launch {
            _cityState.value = CityUiState.Loading
            try {
                val db = Firebase.firestore
                val tripsResult = db.collection("trips")
                    .whereEqualTo("country", countryName)
                    .get()
                    .await()
                val citiesFromTrips = tripsResult.toObjects(Trip::class.java)
                    .mapNotNull { it.cityId }
                    .map { formatCityName(it) }
                    .distinct()
                    .sorted()
                _cityState.value = CityUiState.Success(citiesFromTrips)
            } catch (e: Exception) {
                Log.e("GeoViewModel", "Error fetching cities for country $countryName", e)
                _cityState.value = CityUiState.Error
            }
        }
    }

    fun refreshCountries() {
        getCountries()
    }
}