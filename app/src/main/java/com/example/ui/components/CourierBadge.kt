package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierList

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment

@Composable
fun CourierBadge(
    courierCode: String,
    modifier: Modifier = Modifier
) {
    val courier = CourierList.findByCode(courierCode)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(courier.brandColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("courier_badge_${courier.code}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CourierLogo(courierCode = courier.code, size = 16.dp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = courier.shortName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            softWrap = false,
            maxLines = 1
        )
    }
}

@Composable
fun StatusBadge(
    isDelivered: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDelivered) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)
    val textColor = if (isDelivered) Color(0xFF15803D) else Color(0xFF1D4ED8)
    val text = if (isDelivered) "TERKIRIM" else "PROSES"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("status_badge_${if (isDelivered) "delivered" else "active"}")
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            softWrap = false,
            maxLines = 1
        )
    }
}
