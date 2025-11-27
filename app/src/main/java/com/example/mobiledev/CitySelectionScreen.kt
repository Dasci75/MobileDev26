package com.example.mobiledev

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CitySelectionScreen(navController: NavController, country: String?) {
    val cities = getCitiesForCountry(country) // Replace with your actual data retrieval logic

    Column {
        Text(
            text = "Select a City in $country",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(cities) { city ->
                Text(
                    text = city,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("main/$city")
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}

fun getCitiesForCountry(country: String?): List<String> {
    return when (country) {
        "USA" -> listOf("New York", "Los Angeles", "Chicago")
        "Canada" -> listOf("Toronto", "Vancouver", "Montreal")
        "Mexico" -> listOf("Mexico City", "Cancun", "Guadalajara")
        "Brazil" -> listOf("Rio de Janeiro", "São Paulo", "Salvador")
        "Argentina" -> listOf("Buenos Aires", "Córdoba", "Rosario")
        else -> emptyList()
    }
}
