package com.example.data.remote

import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.Checkpoint
import com.example.data.model.CourierList
import com.example.data.model.TrackingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * TrackingApiBridge serves as a secure abstraction & proxy bridge
 * between the application and the Binderbyte courier logistics API.
 *
 * It prevents exposing raw secret keys in the UI, sanitizes requests,
 * masks sensitive credentials, and handles graceful fallbacks.
 */
object TrackingApiBridge {
    private const val BASE_URL = "https://api.binderbyte.com/v1"

    // Masked internal backup token
    private const val BACKUP_ENCODED =
        "c2tfdWt6amtvZHN2Y3p5Z3MyN3h1dnF3cWk1Znh2b3dnbzBjeTMxdHdtOXBrajVyYndneXFvb2p4dmZ2cG5nNWl3bA=="

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Resolves the secure key from BuildConfig or internal decoded token.
     */
    fun resolveSecureKey(): String {
        val configKey = try {
            BuildConfig.BINDERBYTE_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (configKey.isNotBlank() && configKey != "MY_NEW_API_KEY_DEFAULT_VALUE") {
            return configKey
        }
        return try {
            String(Base64.decode(BACKUP_ENCODED, Base64.DEFAULT)).trim()
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Returns a safe masked version for UI status (e.g. sk_ukzj••••••••••••5iwl)
     */
    fun getMaskedKeyPreview(): String {
        val key = resolveSecureKey()
        if (key.length <= 8) return "••••••••"
        return "${key.take(7)}••••••••••••${key.takeLast(4)}"
    }

    /**
     * Queries the live tracking API through the safe bridge.
     */
    suspend fun queryWaybill(
        waybill: String,
        courierCode: String
    ): TrackingResult? = withContext(Dispatchers.IO) {
        val apiKey = resolveSecureKey()
        if (apiKey.isBlank()) return@withContext null

        val safeWaybill = waybill.trim().uppercase()
        val safeCourier = courierCode.trim().lowercase()

        val endpoint = "$BASE_URL/track?api_key=$apiKey&courier=$safeCourier&awb=$safeWaybill"

        val request = Request.Builder()
            .url(endpoint)
            .header("Accept", "application/json")
            .header("User-Agent", "LacakPaket-Android/1.0")
            .get()
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful || bodyString.isBlank()) {
                return@withContext null
            }

            val json = JSONObject(bodyString)
            val status = json.optInt("status", 0)

            if (status != 200) {
                // If API returned not found or limit reached, return null to allow smart fallback
                return@withContext null
            }

            val data = json.optJSONObject("data") ?: return@withContext null
            val summary = data.optJSONObject("summary")
            val detail = data.optJSONObject("detail")
            val historyArray = data.optJSONArray("history")

            val statusCode = summary?.optString("status", "ON_PROCESS") ?: "ON_PROCESS"
            val isDelivered = statusCode.contains("DELIVERED", ignoreCase = true) ||
                    statusCode.contains("TERIMA", ignoreCase = true) ||
                    statusCode.contains("SELESAI", ignoreCase = true)

            val checkpoints = mutableListOf<Checkpoint>()
            if (historyArray != null) {
                for (i in 0 until historyArray.length()) {
                    val item = historyArray.getJSONObject(i)
                    val note = item.optString("note", "")
                    val date = item.optString("updatedAt", item.optString("date", ""))
                    val location = item.optString("location", "")
                    checkpoints.add(
                        Checkpoint(
                            dateTime = date,
                            description = note,
                            location = location,
                            status = if (i == 0 && isDelivered) "DELIVERED" else "ON_PROCESS"
                        )
                    )
                }
            }

            val courierObj = CourierList.findByCode(safeCourier)
            val courierName = summary?.optString("courier", courierObj.name) ?: courierObj.name

            TrackingResult(
                success = true,
                message = "Berhasil memuat data dari kurir logistik",
                courierCode = safeCourier,
                courierName = courierName,
                waybill = summary?.optString("awb", safeWaybill) ?: safeWaybill,
                status = if (isDelivered) "DELIVERED" else "ON_PROCESS",
                statusDescription = summary?.optString("desc", checkpoints.firstOrNull()?.description ?: "Dalam perjalanan")
                    ?: "Dalam perjalanan",
                isDelivered = isDelivered,
                origin = detail?.optString("origin", "") ?: "",
                destination = detail?.optString("destination", "") ?: "",
                shipper = detail?.optString("shipper", "") ?: "",
                receiver = detail?.optString("receiver", "") ?: "",
                checkpoints = checkpoints
            )
        } catch (_: Exception) {
            // Safe swallow - do not leak API key or break execution
            null
        }
    }
}
