package com.example.mobiledev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.ui.theme.MobileDevTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.example.mobiledev.ui.TripViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

@Composable
fun SettingsScreen(
    navController: NavController,
    auth: FirebaseAuth,
    paddingValues: PaddingValues // Added paddingValues parameter
) {
    Column(
        modifier = Modifier
            .padding(paddingValues) // Apply padding from parent Scaffold
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // Light gray background
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoutButton(navController = navController, auth = auth)
    }
}

@Composable
fun LogoutButton(navController: NavController, auth: FirebaseAuth) {
    Button(
        onClick = {
            auth.signOut()
            navController.navigate("login") {
                // Clear the back stack to prevent going back to the settings screen
                popUpTo(0) { inclusive = true }
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
    ) {
        Text(text = "Log out", color = Color.White, fontSize = 18.sp)
    }
}

