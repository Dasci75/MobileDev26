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

import com.example.mobiledev.ui.GeoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.mobiledev.ui.CountryUiState
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySelectionScreen(
    navController: NavController,
    geoViewModel: GeoViewModel = viewModel(),
    paddingValues: PaddingValues // Added paddingValues parameter
) {
    val uiState by geoViewModel.countryState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), // Apply padding from parent Scaffold
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is CountryUiState.Loading -> CircularProgressIndicator()
            is CountryUiState.Error -> Text("Error: Could not load countries.")
            is CountryUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = uiState is CountryUiState.Loading,
                    onRefresh = { geoViewModel.refresh() }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5)),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.countries) { country ->
                            CountryItem(country = country, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryItem(country: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("citySelection/$country") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = country,
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
