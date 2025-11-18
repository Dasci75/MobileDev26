package com.example.mobiledev

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobiledev.ui.theme.MobileDevTheme

@Composable
fun TripDetailsScreen(tripId: String?, navController: NavController) {
    val trip = dummyTrips.find { it.id.toString() == tripId }

    if (trip == null) {
        // Handle trip not found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Trip not found")
        }
        return
    }

    Scaffold(
        topBar = { DetailTopBar(trip = trip) },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            TripDetailsContent(trip = trip)
        }
    }
}

@Composable
fun DetailTopBar(trip: Trip) {
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
            text = trip.title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TripDetailsContent(trip: Trip) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Main Image
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Placeholder for the image
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Small Images
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Placeholder for small image 1
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
                // Placeholder for small image 2
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
                 // Placeholder for small image 3
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
            }
            // Address Box
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Adres en info", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France", fontSize = 10.sp)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "De Eiffeltoren is een van de meest iconische bezienswaardigheden ter wereld. Hij torent hoog uit in het hart van Parijs en biedt adembenemende uitzichten over de stad vanaf elk niveau. Het voelde bijna onwerkelijk om hem te bezoeken – vooral wanneer hij 's avonds schitterde in het licht! De klim naar de top was een onvergetelijke ervaring, en het uitzicht over Parijs was gewoon geweldig.",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Review Button
        Button(
            onClick = { /* TODO: Handle Review action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825))
        ) {
            Text(text = "Review", color = Color.White, fontSize = 18.sp)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TripDetailsScreenPreview() {
    MobileDevTheme {
        TripDetailsScreen(tripId = "1", navController = rememberNavController())
    }
}
