package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Courier

@Composable
fun JneLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF00569B)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JNE",
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Box(
                modifier = Modifier
                    .width(size * 0.55f)
                    .height(2.dp)
                    .background(Color(0xFFE51B24), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun JntLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE50012)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "J&T",
            color = Color.White,
            fontSize = (size.value * 0.40f).sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun SiCepatLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFD9222A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val path = Path().apply {
                moveTo(this@Canvas.size.width * 0.55f, 0f)
                lineTo(this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.55f)
                lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.55f)
                lineTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height)
                lineTo(this@Canvas.size.width * 0.85f, this@Canvas.size.height * 0.42f)
                lineTo(this@Canvas.size.width * 0.50f, this@Canvas.size.height * 0.42f)
                close()
            }
            drawPath(path, color = Color(0xFFFFC107))
        }
    }
}

@Composable
fun AnterAjaLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE6007E)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "a",
                color = Color.White,
                fontSize = (size.value * 0.52f).sp,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .size(size * 0.18f)
                    .background(Color(0xFFFFD54F), CircleShape)
            )
        }
    }
}

@Composable
fun PosIndoLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFFF6600)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val p = Path().apply {
                moveTo(this@Canvas.size.width * 0.92f, this@Canvas.size.height * 0.32f)
                lineTo(this@Canvas.size.width * 0.78f, this@Canvas.size.height * 0.20f)
                lineTo(this@Canvas.size.width * 0.55f, this@Canvas.size.height * 0.05f)
                lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.25f)
                lineTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.10f)
                lineTo(this@Canvas.size.width * 0.25f, this@Canvas.size.height * 0.35f)
                lineTo(this@Canvas.size.width * 0.05f, this@Canvas.size.height * 0.45f)
                lineTo(this@Canvas.size.width * 0.20f, this@Canvas.size.height * 0.60f)
                lineTo(this@Canvas.size.width * 0.55f, this@Canvas.size.height * 0.72f)
                lineTo(this@Canvas.size.width * 0.85f, this@Canvas.size.height * 0.50f)
                close()
            }
            drawPath(p, color = Color.White)
        }
    }
}

@Composable
fun NinjaLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFC8102E)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.70f, size * 0.45f)) {
            drawRoundRect(
                color = Color(0xFF1E1E1E),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            val leftEye = Path().apply {
                moveTo(this@Canvas.size.width * 0.20f, this@Canvas.size.height * 0.65f)
                lineTo(this@Canvas.size.width * 0.42f, this@Canvas.size.height * 0.30f)
                lineTo(this@Canvas.size.width * 0.38f, this@Canvas.size.height * 0.75f)
                close()
            }
            drawPath(leftEye, color = Color.White)

            val rightEye = Path().apply {
                moveTo(this@Canvas.size.width * 0.80f, this@Canvas.size.height * 0.65f)
                lineTo(this@Canvas.size.width * 0.58f, this@Canvas.size.height * 0.30f)
                lineTo(this@Canvas.size.width * 0.62f, this@Canvas.size.height * 0.75f)
                close()
            }
            drawPath(rightEye, color = Color.White)
        }
    }
}

@Composable
fun LionLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFED1C24)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val wing = Path().apply {
                moveTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.90f)
                lineTo(this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.25f)
                lineTo(this@Canvas.size.width * 0.35f, this@Canvas.size.height * 0.45f)
                lineTo(this@Canvas.size.width * 0.5f, this@Canvas.size.height * 0.10f)
                lineTo(this@Canvas.size.width * 0.65f, this@Canvas.size.height * 0.45f)
                lineTo(this@Canvas.size.width * 0.85f, this@Canvas.size.height * 0.25f)
                close()
            }
            drawPath(wing, color = Color(0xFFFFD54F))
            drawCircle(color = Color.White, radius = this@Canvas.size.width * 0.16f, center = center)
        }
    }
}

@Composable
fun IdExpressLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFC90022)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "i",
                color = Color(0xFFFACC15),
                fontSize = (size.value * 0.48f).sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "D",
                color = Color.White,
                fontSize = (size.value * 0.44f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun SpxLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFEE4D2D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SPX",
            color = Color.White,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun WahanaLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFD200)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "W",
                color = Color(0xFF0038A8),
                fontSize = (size.value * 0.45f).sp,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .width(size * 0.45f)
                    .height(2.dp)
                    .background(Color(0xFF0038A8), RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun TikiLogo(size: Dp = 26.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF005DAA)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TIKI",
                color = Color.White,
                fontSize = (size.value * 0.32f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.width(1.dp))
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Box(modifier = Modifier.size(size * 0.11f).background(Color(0xFF00A859), CircleShape))
                Box(modifier = Modifier.size(size * 0.11f).background(Color(0xFFFFD54F), CircleShape))
            }
        }
    }
}

/**
 * Universal dispatcher for any courier brand logo.
 */
@Composable
fun CourierLogo(
    courierCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Box(modifier = modifier) {
        when (courierCode.lowercase()) {
            "jne" -> JneLogo(size = size)
            "jnt" -> JntLogo(size = size)
            "sicepat" -> SiCepatLogo(size = size)
            "anteraja" -> AnterAjaLogo(size = size)
            "pos" -> PosIndoLogo(size = size)
            "ninja" -> NinjaLogo(size = size)
            "lion" -> LionLogo(size = size)
            "idexpress" -> IdExpressLogo(size = size)
            "spx" -> SpxLogo(size = size)
            "wahana" -> WahanaLogo(size = size)
            "tiki" -> TikiLogo(size = size)
            else -> {
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = courierCode.take(3).uppercase(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Official Brand Selection Button Chip with Logo, Brand Typography, and Selection State
 */
@Composable
fun CourierBrandButton(
    courier: Courier,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) courier.brandColor else Color(0xFFE2E8F0)
    val backgroundColor = if (isSelected) {
        courier.brandColor.copy(alpha = 0.10f)
    } else {
        Color.White
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier
            .testTag("courier_select_${courier.code}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CourierLogo(courierCode = courier.code, size = 22.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = courier.shortName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) courier.brandColor else Color(0xFF1E293B)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Terpilih",
                    tint = courier.brandColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
