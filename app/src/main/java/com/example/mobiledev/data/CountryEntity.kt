package com.example.mobiledev.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val id: String,
    val name: String
)
