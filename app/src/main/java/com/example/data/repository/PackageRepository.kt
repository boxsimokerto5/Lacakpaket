package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.AppDatabase
import com.example.data.entity.TrackedPackageEntity
import com.example.data.model.CourierList
import com.example.data.model.TrackingResult
import com.example.data.remote.TrackingApiBridge
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PackageRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val packageDao = db.packageDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lacak_paket_prefs", Context.MODE_PRIVATE)

    val allPackages: Flow<List<TrackedPackageEntity>> = packageDao.getAllPackages()

    fun getPackageById(id: Long): Flow<TrackedPackageEntity?> = packageDao.getPackageById(id)

    fun getApiKey(): String = prefs.getString("binderbyte_api_key", "") ?: ""

    fun setApiKey(key: String) {
        prefs.edit().putString("binderbyte_api_key", key.trim()).apply()
    }

    fun isNotificationEnabled(): Boolean = prefs.getBoolean("notif_enabled", true)

    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
    }

    fun getSyncIntervalMinutes(): Int = prefs.getInt("sync_interval", 30)

    fun setSyncIntervalMinutes(interval: Int) {
        prefs.edit().putInt("sync_interval", interval).apply()
    }

    suspend fun savePackage(entity: TrackedPackageEntity): Long = withContext(Dispatchers.IO) {
        val existing = packageDao.findPackage(entity.waybill, entity.courierCode)
        if (existing != null) {
            val updated = entity.copy(id = existing.id)
            packageDao.update(updated)
            existing.id
        } else {
            packageDao.insert(entity)
        }
    }

    suspend fun updatePackage(entity: TrackedPackageEntity) = withContext(Dispatchers.IO) {
        packageDao.update(entity)
    }

    suspend fun deletePackage(id: Long) = withContext(Dispatchers.IO) {
        packageDao.deleteById(id)
    }

    suspend fun deleteAllPackages() = withContext(Dispatchers.IO) {
        packageDao.deleteAll()
    }

    /**
     * Fetch authentic tracking information from live logistics API.
     * No dummy or simulated data is ever returned.
     */
    suspend fun fetchTrackingInfo(
        waybill: String,
        courierCode: String,
        customTitle: String = ""
    ): TrackingResult = withContext(Dispatchers.IO) {
        val userApiKey = getApiKey()
        TrackingApiBridge.queryWaybill(
            waybill = waybill,
            courierCode = courierCode,
            customApiKey = userApiKey
        )
    }

    /**
     * Checks all active packages for authentic updates.
     * Only triggers notifications if genuine status progression occurs.
     */
    suspend fun checkAllActivePackages(): Int = withContext(Dispatchers.IO) {
        val activePackages = packageDao.getActivePackages()
        var updatedCount = 0

        for (pkg in activePackages) {
            val result = fetchTrackingInfo(pkg.waybill, pkg.courierCode, pkg.customTitle)
            if (result.success && result.statusDescription != pkg.lastNotifiedCheckpoint) {
                val updated = pkg.copy(
                    status = result.status,
                    statusDescription = result.statusDescription,
                    isDelivered = result.isDelivered,
                    origin = result.origin.ifBlank { pkg.origin },
                    destination = result.destination.ifBlank { pkg.destination },
                    shipper = result.shipper.ifBlank { pkg.shipper },
                    receiver = "",
                    lastUpdated = System.currentTimeMillis(),
                    lastNotifiedCheckpoint = result.statusDescription,
                    checkpointsJson = TrackedPackageEntity.checkpointsToJson(result.checkpoints)
                )
                packageDao.update(updated)
                updatedCount++

                if (isNotificationEnabled()) {
                    NotificationHelper.sendPackageStatusNotification(
                        context = context,
                        packageTitle = pkg.customTitle,
                        waybill = pkg.waybill,
                        courierName = pkg.courierName,
                        statusDesc = result.statusDescription,
                        isDelivered = result.isDelivered
                    )
                }
            }
        }

        updatedCount
    }
}
