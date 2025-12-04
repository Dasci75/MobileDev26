package com.example.mobiledev.data

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "mobiledev-database"
        ).build()
    }

    fun provideCountryDao(database: AppDatabase): CountryDao {
        return database.countryDao()
    }

    fun provideCityDao(database: AppDatabase): CityDao {
        return database.cityDao()
    }
}
