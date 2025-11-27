package com.example.mobiledev.data

data class Trip(
    val id: String = "",
    val title: String? = null,
    val location: String? = null,
    val rating: Double? = null,
    val country: String? = null,
    val cityId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
