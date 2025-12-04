package com.example.mobiledev.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledev.data.AppDatabase
import com.example.mobiledev.data.DatabaseModule

class GeoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeoViewModel::class.java)) {
            val database = DatabaseModule.provideDatabase(application)
            val countryDao = DatabaseModule.provideCountryDao(database)
            val cityDao = DatabaseModule.provideCityDao(database)
            @Suppress("UNCHECKED_CAST")
            return GeoViewModel(application, countryDao, cityDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
