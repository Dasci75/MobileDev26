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
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiledev.ui.CityUiState
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import com.example.mobiledev.ui.GeoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripCitySelectionScreen(
    navController: NavController,
    countryName: String?,
    paddingValues: PaddingValues,
    geoViewModel: GeoViewModel = viewModel()
) {
    if (countryName == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Apply padding from parent Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text("Country not specified.")
        }
        return
    }

    LaunchedEffect(countryName) {
        geoViewModel.getCities(countryName)
    }

    val uiState by geoViewModel.cityState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), // Apply padding from parent Scaffold
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("addCity/$countryName") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add City")
        }

        when (val state = uiState) {
            is CityUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is CityUiState.Error -> {
                // Display nothing here.
            }
            is CityUiState.Success -> {
                if (state.cities.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No cities found for this country. Add one!")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.cities) { city ->
                            AddTripCityItem(city = city, navController = navController, country = countryName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTripCityItem(city: String, navController: NavController, country: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedCity", city)
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedCountry", country)
                navController.popBackStack()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = city.replaceFirstChar { it.uppercaseChar() },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
