package com.example.mobiledev

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CitySelectionScreen(navController: NavController, country: String?) {
    // Dummy data - in a real app, this would come from a database or API
    val citiesByCountry = mapOf(
        "Italy" to listOf("Rome", "Florence", "Venice"),
        "France" to listOf("Paris", "Nice", "Lyon"),
        "Spain" to listOf("Madrid", "Barcelona", "Seville"),
        "USA" to listOf("New York", "Los Angeles", "Chicago"),
        "Japan" to listOf("Tokyo", "Kyoto", "Osaka")
    )

    val cities = citiesByCountry[country] ?: emptyList()

    Scaffold(
        topBar = { CitySelectionTopBar(country = country) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cities) { city ->
                CityItem(city = city, navController = navController)
            }
        }
    }
}

@Composable
fun CitySelectionTopBar(country: String?) {
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
            text = "Select a City in ${country ?: ""}",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CityItem(city: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                navController.navigate("main?city=$city") {
                    // Go back to main screen, don't add to back stack
                    popUpTo("main") { inclusive = true }
                }
             },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = city,
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
