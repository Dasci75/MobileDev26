package com.example.mobiledev.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Home : Screen("main", Icons.Default.Home, "Home")
    object Dashboard : Screen("dashboard", Icons.Default.Star, "Dashboard")
    object Chat : Screen("chat", Icons.AutoMirrored.Filled.Chat, "Chat")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
}
