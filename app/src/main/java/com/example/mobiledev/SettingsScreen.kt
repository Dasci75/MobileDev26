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

@Composable
fun SettingsScreen(navController: NavController, auth: FirebaseAuth) {
    Scaffold(
        topBar = { SettingsTopBar() },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)), // Light gray background
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoutButton(navController = navController, auth = auth)
        }
    }
}

@Composable
fun SettingsTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9A825)) // Orange
            .padding(16.dp)
    ) {
        Text(
            text = "CityTrip",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MobileDevTheme {
        SettingsScreen(rememberNavController(), Firebase.auth)
    }
}
