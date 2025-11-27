package com.example.mobiledev.data

import com.google.firebase.Timestamp

data class Trip(
    val id: String = "",
    val name: String? = null, // Changed from title to name
    val location: String? = null,
    val rating: Double? = null,
    val country: String? = null,
    val cityId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    val createdAt: Timestamp? = null,
    val description: String? = null,
    val photo1: String? = null,
    val photo2: String? = null,
    val photo3: String? = null,
    val photo4: String? = null
)
