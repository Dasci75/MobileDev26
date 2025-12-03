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
import com.example.mobiledev.ui.CityViewModel
import com.example.mobiledev.ui.CityViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobiledev.ui.CityUiState
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionScreen(
    navController: NavController,
    countryName: String?,
    cityViewModel: CityViewModel,
    paddingValues: PaddingValues // Added paddingValues parameter
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

    val uiState by cityViewModel.cityState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), // Apply padding from parent Scaffold
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is CityUiState.Loading -> CircularProgressIndicator()
            is CityUiState.Error -> Text("Error: Could not load cities.")
            is CityUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState is CityUiState.Loading,
                    onRefresh = { cityViewModel.refresh() }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.cities) { city ->
                            CityItem(city = city, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityItem(city: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("main?city=$city")
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
