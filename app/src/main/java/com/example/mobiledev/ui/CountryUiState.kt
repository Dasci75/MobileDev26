package com.example.mobiledev.ui

sealed interface CountryUiState {
    data class Success(val countries: List<String>) : CountryUiState
    object Error : CountryUiState
    object Loading : CountryUiState
}
