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

@Composable
fun AddCityScreen(
    navController: NavController,
    countryName: String?,
    from: String?
) {
    if (countryName == null) {

        // Handle error: countryName is required
        return
    }

    var cityName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = cityName,
            onValueChange = { cityName = it },
            label = { Text("City Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (cityName.isNotBlank()) {
                    if (from == "addTrip") {
                        navController.getBackStackEntry("addTrip").savedStateHandle.set("selectedCity", cityName)
                        navController.getBackStackEntry("addTrip").savedStateHandle.set("isNewCity", true)
                        navController.popBackStack("addTrip", inclusive = false)
                    } else if (from == "addTripCitySelection") {
                        navController.getBackStackEntry("addTrip").savedStateHandle.set("selectedCity", cityName)
                        navController.getBackStackEntry("addTrip").savedStateHandle.set("isNewCity", true)
                        navController.popBackStack("addTrip", inclusive = false)
                    } else if (from == "citySelection") {
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedCity", cityName)
                        navController.previousBackStackEntry?.savedStateHandle?.set("isNewCity", true)
                        navController.popBackStack()
                    }
                }
            }
        ) {
            Text("Save City")
        }
    }
}
