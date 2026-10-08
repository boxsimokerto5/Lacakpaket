package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TrackedPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageDao {
    @Query("SELECT * FROM tracked_packages ORDER BY lastUpdated DESC")
    fun getAllPackages(): Flow<List<TrackedPackageEntity>>

    @Query("SELECT * FROM tracked_packages WHERE id = :id LIMIT 1")
    fun getPackageById(id: Long): Flow<TrackedPackageEntity?>

    @Query("SELECT * FROM tracked_packages WHERE waybill = :waybill AND courierCode = :courierCode LIMIT 1")
    suspend fun findPackage(waybill: String, courierCode: String): TrackedPackageEntity?

    @Query("SELECT * FROM tracked_packages WHERE isDelivered = 0")
    suspend fun getActivePackages(): List<TrackedPackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TrackedPackageEntity): Long

    @Update
    suspend fun update(item: TrackedPackageEntity)

    @Delete
    suspend fun delete(item: TrackedPackageEntity)

    @Query("DELETE FROM tracked_packages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tracked_packages")
    suspend fun deleteAll()
}
