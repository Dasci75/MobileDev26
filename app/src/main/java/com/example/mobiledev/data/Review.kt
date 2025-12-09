package com.example.mobiledev.data

import com.google.firebase.Timestamp

data class Review(
    val userId: String = "",
    val userEmail: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Timestamp? = null
)
