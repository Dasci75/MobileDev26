package com.example.mobiledev.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Double,
    stars: Int = 5,
    starSize: Dp = 16.dp,
    onRatingChanged: ((Double) -> Unit)? = null
) {
    val starColor = Color(0xFFF9A825) // A gold-like color, more visible than white

    Row(modifier = modifier) {
        for (i in 1..stars) {
            val iconModifier = if (onRatingChanged != null) {
                Modifier.clickable { onRatingChanged(i.toDouble()) }
            } else {
                Modifier
            }
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Star",
                tint = if (i <= rating) starColor else Color.Gray,
                modifier = iconModifier.size(starSize)
            )
        }
    }
}
