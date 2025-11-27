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
fun CountrySelectionScreen(navController: NavController) {
    val countries = listOf("USA", "Canada", "Mexico", "Brazil", "Argentina") // Replace with your actual list of countries

    Column {
        Text(
            text = "Select a Country",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(countries) { country ->
                Text(
                    text = country,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("citySelection/$country")
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}
