package com.example.mobiledev

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobiledev.ui.GeoViewModel

@Composable
fun AddCountryScreen(
    navController: NavController,
    geoViewModel: GeoViewModel
) {
    var countryName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = countryName,
            onValueChange = { countryName = it },
            label = { Text("Country Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (countryName.isNotBlank()) {
                    geoViewModel.addCountry(countryName)
                    navController.popBackStack()
                }
            }
        ) {
            Text("Save Country")
        }
    }
}
