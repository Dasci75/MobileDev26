package com.example.mobiledev.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<CountryEntity>)

    @Query("SELECT * FROM countries")
    fun getAllCountries(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE id = :countryId")
    fun getCountryById(countryId: String): Flow<CountryEntity>

    @Query("SELECT * FROM countries WHERE name = :name")
    suspend fun getCountryByName(name: String): CountryEntity?
}
