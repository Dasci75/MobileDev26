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

private const val TAG = "DashboardScreen" // Define TAG

@Composable
fun DashboardScreen(paddingValues: PaddingValues) {
    Log.d(TAG, "DashboardScreen is composing") // Log statement
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text("Dashboard Screen")
    }
}
