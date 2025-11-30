package com.example.mobiledev.data

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false
)
