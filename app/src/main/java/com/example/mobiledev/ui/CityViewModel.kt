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
import com.google.firebase.firestore.Source

sealed interface CityUiState {
    data class Success(val cities: List<String>) : CityUiState
    object Error : CityUiState
    object Loading : CityUiState
}

class CityViewModel(private val countryName: String) : ViewModel() {

    private val _cityState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val cityState: StateFlow<CityUiState> = _cityState.asStateFlow()

    init {
        getCities(Source.DEFAULT)
    }

    fun getCities(source: Source) {
        viewModelScope.launch {
            _cityState.value = CityUiState.Loading
            try {
                val db = Firebase.firestore
                val result = db.collection("trips").whereEqualTo("country", countryName).get(source).await()
                val cities = result.toObjects(Trip::class.java).mapNotNull { it.cityId }.distinct()
                _cityState.value = CityUiState.Success(cities)
            } catch (e: Exception) {
                Log.e("CityViewModel", "Error fetching cities for country $countryName", e)
                _cityState.value = CityUiState.Error
            }
        }
    }

    fun refresh() {
        getCities(Source.SERVER)
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
