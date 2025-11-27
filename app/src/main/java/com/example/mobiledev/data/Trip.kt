package com.example.mobiledev.data

import com.google.firebase.Timestamp

data class Trip(
    val id: String = "",
    val name: String? = null,
    val rating: Double? = null,
    val country: String? = null,
    val cityId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val category: String? = null,
    val createdAt: Timestamp? = null,
    val description: String? = null,
    val photoUrl: Map<String, String>? = null // Nested map for photo URLs
)
