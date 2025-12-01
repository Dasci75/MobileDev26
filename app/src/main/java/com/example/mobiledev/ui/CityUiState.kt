package com.example.mobiledev.ui

sealed interface CityUiState {
    data class Success(val cities: List<String>) : CityUiState
    object Error : CityUiState
    object Loading : CityUiState
}
