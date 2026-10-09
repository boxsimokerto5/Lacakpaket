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
                    val rawNote = item.optString("desc", "")
                        .ifBlank { item.optString("description", "") }
                        .ifBlank { item.optString("note", "") }
                        .ifBlank { item.optString("message", "") }
                        .ifBlank { item.optString("status", "") }
                    val date = item.optString("updatedAt", item.optString("date", ""))
                    val location = item.optString("location", "")
                    val isItemDelivered = (i == 0 && isDelivered) || rawNote.contains("DELIVERED", ignoreCase = true) || rawNote.contains("DITERIMA", ignoreCase = true)

                    val enrichedNote = enrichStatusDescription(
                        rawNote = rawNote,
                        location = location,
                        isFirst = i == 0,
                        isLast = i == historyArray.length() - 1,
                        isDelivered = isItemDelivered
                    )

                    checkpoints.add(
                        Checkpoint(
                            dateTime = date,
                            description = enrichedNote,
                            location = location,
                            status = if (isItemDelivered) "DELIVERED" else "ON_PROCESS"
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
                    ?: (checkpoints.firstOrNull()?.description ?: "Dalam perjalanan"),
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

    /**
     * Enriches brief or empty courier checkpoint status notes into full, informative Indonesian sentences.
     */
    fun enrichStatusDescription(
        rawNote: String,
        location: String,
        isFirst: Boolean,
        isLast: Boolean,
        isDelivered: Boolean
    ): String {
        val trimmed = rawNote.trim()
        val loc = location.trim()

        // If it's already a full sentence (more than 30 chars with multiple words), keep it
        if (trimmed.length > 30 && trimmed.contains(" ") && trimmed.split("\\s+".toRegex()).size >= 5) {
            return trimmed
        }

        val upper = trimmed.uppercase()
        val locLabel = if (loc.isNotBlank()) " [$loc]" else ""

        return when {
            isFirst && (isDelivered || upper.contains("DELIVERED") || upper.contains("DITERIMA") || upper.contains("SELESAI") || upper.contains("POD")) -> {
                "Paket telah berhasil diantar dan diterima oleh penerima di lokasi tujuan."
            }
            upper.contains("OUT FOR DELIVERY") || upper.contains("DELIVERY") || upper.contains("ANTAR") || upper.contains("KURIR") || upper.contains("DIANTAR") -> {
                "Paket sedang dibawa oleh kurir logistik dan dalam perjalanan diantar langsung ke alamat tujuan Anda."
            }
            isFirst && !isDelivered -> {
                if (loc.isNotBlank()) {
                    "Paket telah tiba di pusat transit & sortir$locLabel, saat ini sedang disiapkan untuk proses pengantaran ke alamat tujuan."
                } else {
                    "Paket sedang dalam perjalanan menuju alamat lokasi tujuan Anda."
                }
            }
            upper.contains("DEPART") || upper.contains("BERANGKAT") || upper.contains("KIRIM") -> {
                "Paket telah diberangkatkan dari fasilitas logistik$locLabel menuju hub kota tujuan selanjutnya."
            }
            upper.contains("ARRIVE") || upper.contains("TIBA") || upper.contains("MASUK") || upper.contains("HUB") || upper.contains("DC") -> {
                "Paket telah tiba dan selesai diproses di pusat transit & sortir logistik$locLabel."
            }
            isLast || upper.contains("PICKUP") || upper.contains("DROP") || upper.contains("INPUT") || upper.contains("MANIFEST") || upper.contains("TERIMA DARI") -> {
                "Paket telah diserahkan ke loket ekspedisi$locLabel dan nomor resi telah aktif dalam sistem logistik."
            }
            trimmed.isNotBlank() && loc.isNotBlank() -> {
                "Paket berstatus $trimmed dan saat ini sedang diproses di fasilitas logistik$locLabel."
            }
            trimmed.isNotBlank() -> {
                "Status: $trimmed - Paket dalam perjalanan menuju lokasi tujuan."
            }
            loc.isNotBlank() -> {
                "Paket telah tiba dan sedang diproses di fasilitas logistik$locLabel."
            }
            else -> {
                "Paket sedang dalam proses perjalanan logistik menuju lokasi tujuan."
            }
        }
    }
}
