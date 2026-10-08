package com.example.data.model

import androidx.compose.ui.graphics.Color

data class Courier(
    val code: String,
    val name: String,
    val shortName: String,
    val brandColor: Color,
    val prefixHint: String = "",
    val website: String = ""
)

object CourierList {
    val supportedCouriers = listOf(
        Courier(
            code = "jne",
            name = "JNE Express",
            shortName = "JNE",
            brandColor = Color(0xFF00569B),
            prefixHint = "01, 02, atau nomor resi 12-16 digit",
            website = "https://www.jne.co.id"
        ),
        Courier(
            code = "jnt",
            name = "J&T Express",
            shortName = "J&T",
            brandColor = Color(0xFFE50012),
            prefixHint = "JP, JX, atau 12 digit angka",
            website = "https://www.jet.co.id"
        ),
        Courier(
            code = "sicepat",
            name = "SiCepat Ekspres",
            shortName = "SiCepat",
            brandColor = Color(0xFFD9222A),
            prefixHint = "000, 001, atau 12 digit",
            website = "https://www.sicepat.com"
        ),
        Courier(
            code = "anteraja",
            name = "AnterAja",
            shortName = "AnterAja",
            brandColor = Color(0xFFE6007E),
            prefixHint = "100, 101, atau 13 digit",
            website = "https://anteraja.id"
        ),
        Courier(
            code = "pos",
            name = "Pos Indonesia",
            shortName = "Pos Indo",
            brandColor = Color(0xFFFF6600),
            prefixHint = "P, EE, atau 11 digit",
            website = "https://www.posindonesia.co.id"
        ),
        Courier(
            code = "ninja",
            name = "Ninja Xpress",
            shortName = "Ninja",
            brandColor = Color(0xFFC8102E),
            prefixHint = "NLID, SHP, atau alphanumeric",
            website = "https://www.ninjaxpress.co"
        ),
        Courier(
            code = "lion",
            name = "Lion Parcel",
            shortName = "Lion",
            brandColor = Color(0xFFED1C24),
            prefixHint = "LP, 11-digit",
            website = "https://lionparcel.com"
        ),
        Courier(
            code = "idexpress",
            name = "IDexpress",
            shortName = "IDexpress",
            brandColor = Color(0xFFC90022),
            prefixHint = "IDS, IDE",
            website = "https://idexpress.com"
        ),
        Courier(
            code = "spx",
            name = "Shopee Xpress (SPX)",
            shortName = "SPX",
            brandColor = Color(0xFFEE4D2D),
            prefixHint = "SPXID, ID...",
            website = "https://spx.co.id"
        ),
        Courier(
            code = "wahana",
            name = "Wahana Express",
            shortName = "Wahana",
            brandColor = Color(0xFF0038A8),
            prefixHint = "8 digit angka",
            website = "https://www.wahana.com"
        ),
        Courier(
            code = "tiki",
            name = "TIKI",
            shortName = "TIKI",
            brandColor = Color(0xFF005DAA),
            prefixHint = "660, 030...",
            website = "https://tiki.id"
        )
    )

    fun findByCode(code: String): Courier {
        return supportedCouriers.find { it.code.equals(code, ignoreCase = true) }
            ?: Courier(
                code = code,
                name = code.uppercase(),
                shortName = code.uppercase(),
                brandColor = Color(0xFF3F51B5)
            )
    }

    fun detectCourier(waybill: String): Courier? {
        val trimmed = waybill.trim().uppercase()
        return when {
            trimmed.startsWith("JP") || trimmed.startsWith("JX") -> findByCode("jnt")
            trimmed.startsWith("SPX") -> findByCode("spx")
            trimmed.startsWith("NLID") -> findByCode("ninja")
            trimmed.startsWith("LP") -> findByCode("lion")
            trimmed.startsWith("IDS") || trimmed.startsWith("IDE") -> findByCode("idexpress")
            trimmed.startsWith("000") || trimmed.startsWith("001") || trimmed.startsWith("002") -> findByCode("sicepat")
            trimmed.startsWith("100") || trimmed.startsWith("101") -> findByCode("anteraja")
            trimmed.startsWith("01") && trimmed.length >= 12 -> findByCode("jne")
            else -> null
        }
    }
}
