package com.example.mobiledev.data

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val userIds: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val tripId: String? = null,
    val createdAt: Timestamp? = null,
    val chatName: String = "",
    val chatPhotoUrl: String = ""
)
