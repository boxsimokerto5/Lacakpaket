package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Checkpoint
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "tracked_packages")
data class TrackedPackageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val waybill: String,
    val courierCode: String,
    val courierName: String,
    val customTitle: String = "",
    val status: String = "ON_PROCESS",
    val statusDescription: String = "",
    val isDelivered: Boolean = false,
    val origin: String = "",
    val destination: String = "",
    val shipper: String = "",
    val receiver: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastNotifiedCheckpoint: String = "",
    val checkpointsJson: String = "[]"
) {
    fun parseCheckpoints(): List<Checkpoint> {
        return try {
            val jsonArray = JSONArray(checkpointsJson)
            val list = mutableListOf<Checkpoint>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Checkpoint(
                        dateTime = obj.optString("dateTime", ""),
                        description = obj.optString("description", ""),
                        location = obj.optString("location", ""),
                        status = obj.optString("status", "ON_PROCESS")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        fun checkpointsToJson(checkpoints: List<Checkpoint>): String {
            val jsonArray = JSONArray()
            for (cp in checkpoints) {
                val obj = JSONObject()
                obj.put("dateTime", cp.dateTime)
                obj.put("description", cp.description)
                obj.put("location", cp.location)
                obj.put("status", cp.status)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }
    }
}
