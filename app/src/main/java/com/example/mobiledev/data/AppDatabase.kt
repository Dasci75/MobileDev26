package com.example.mobiledev.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CountryEntity::class, CityEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun cityDao(): CityDao
}
