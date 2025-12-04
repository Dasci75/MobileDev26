package com.example.mobiledev.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val countryId: String,
    val name: String
)
