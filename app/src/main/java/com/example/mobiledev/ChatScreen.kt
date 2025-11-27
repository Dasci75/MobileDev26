package com.example.mobiledev

import android.util.Log // Import Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

private const val TAG = "ChatScreen" // Define TAG

@Composable
fun ChatScreen(paddingValues: PaddingValues) {
    Log.d(TAG, "ChatScreen is composing") // Log statement
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text("Chat Screen")
    }
}
