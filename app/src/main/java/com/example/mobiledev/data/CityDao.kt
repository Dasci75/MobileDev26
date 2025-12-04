package com.example.mobiledev.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Query("SELECT * FROM cities WHERE countryId = :countryId")
    fun getCitiesForCountry(countryId: String): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    fun getCityById(cityId: String): Flow<CityEntity>

    @Query("SELECT * FROM cities WHERE name = :name AND countryId = :countryId")
    suspend fun getCityByNameAndCountry(name: String, countryId: String): CityEntity?
}
