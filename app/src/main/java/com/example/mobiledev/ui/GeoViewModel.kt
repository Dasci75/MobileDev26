package com.example.mobiledev.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.CityDao
import com.example.mobiledev.data.CityEntity
import com.example.mobiledev.data.CountryDao
import com.example.mobiledev.data.CountryEntity
import com.example.mobiledev.data.Trip
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.Normalizer


class GeoViewModel(application: Application, private val countryDao: CountryDao, private val cityDao: CityDao) : AndroidViewModel(application) {

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
                // Try to load from Room first
                val cachedCountries = countryDao.getAllCountries().firstOrNull()
                if (!cachedCountries.isNullOrEmpty()) {
                    _countryState.value = CountryUiState.Success(cachedCountries.map { it.name }.sorted())
                    Log.d("GeoViewModel", "Loaded countries from Room: ${cachedCountries.size}")
                }

                // Fetch from Firebase
                val db = Firebase.firestore
                val result = db.collection("trips").get().await()
                val firebaseCountries = result.documents.mapNotNull { it.getString("country") }.distinct().sorted()

                if (firebaseCountries.isNotEmpty()) {
                    // Save to Room
                    val countryEntities = firebaseCountries.map { CountryEntity(id = it, name = it) }
                    countryDao.insertAll(countryEntities)
                    _countryState.value = CountryUiState.Success(firebaseCountries)
                    Log.d("GeoViewModel", "Loaded countries from Firebase and saved to Room: ${firebaseCountries.size}")
                } else if (cachedCountries.isNullOrEmpty()) {
                    _countryState.value = CountryUiState.Error
                }
            } catch (e: Exception) {
                Log.e("GeoViewModel", "Error fetching countries", e)
                _countryState.value = CountryUiState.Error
            }
        }
    }

    fun getCities(countryName: String) {
        viewModelScope.launch {
            _cityState.value = CityUiState.Loading
            try {
                // Try to load from Room first
                val cachedCities = cityDao.getCitiesForCountry(countryName).firstOrNull()
                if (!cachedCities.isNullOrEmpty()) {
                    _cityState.value = CityUiState.Success(cachedCities.map { it.name }.sorted())
                    Log.d("GeoViewModel", "Loaded cities for $countryName from Room: ${cachedCities.size}")
                }

                // Fetch from Firebase
                val db = Firebase.firestore
                val tripsResult = db.collection("trips")
                    .whereEqualTo("country", countryName)
                    .get()
                    .await()
                val firebaseCities = tripsResult.toObjects(Trip::class.java)
                    .mapNotNull { it.cityId }
                    .map { formatCityName(it) }
                    .distinct()
                    .sorted()

                if (firebaseCities.isNotEmpty()) {
                    // Save to Room
                    val cityEntities = firebaseCities.map { CityEntity(id = "$countryName-${it}", countryId = countryName, name = it) }
                    cityDao.insertAll(cityEntities)
                    _cityState.value = CityUiState.Success(firebaseCities)
                    Log.d("GeoViewModel", "Loaded cities for $countryName from Firebase and saved to Room: ${firebaseCities.size}")
                } else if (cachedCities.isNullOrEmpty()) {
                    _cityState.value = CityUiState.Error
                }
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