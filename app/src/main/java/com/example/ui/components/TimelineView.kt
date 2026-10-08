package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Checkpoint

@Composable
fun DeliveryProgressStepper(
    isDelivered: Boolean,
    checkpointsCount: Int,
    modifier: Modifier = Modifier
) {
    val currentStep = when {
        isDelivered -> 4
        checkpointsCount >= 4 -> 3
        checkpointsCount >= 2 -> 2
        else -> 1
    }

    val steps = listOf(
        Triple("Diproses", Icons.Default.Inventory2, Color(0xFFF59E0B)),
        Triple("Transit", Icons.Default.LocalShipping, Color(0xFF2563EB)),
        Triple("Diantar", Icons.Default.DirectionsCar, Color(0xFF8B5CF6)),
        Triple("Sampai", Icons.Default.CheckCircle, Color(0xFF10B981))
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("delivery_stepper_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "STATUS PERJALANAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (label, icon, color) ->
                    val stepNumber = index + 1
                    val isCompleted = stepNumber <= currentStep
                    val isCurrent = stepNumber == currentStep

                    val circleColor = when {
                        isCompleted -> color
                        else -> Color(0xFFE2E8F0)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(circleColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isCompleted) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isCurrent) color else if (isCompleted) Color(0xFF1E293B) else Color(0xFF94A3B8)
                        )
                    }

                    if (index < steps.size - 1) {
                        val lineColor = if (stepNumber < currentStep) {
                            color
                        } else {
                            Color(0xFFE2E8F0)
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(lineColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineView(
    checkpoints: List<Checkpoint>,
    modifier: Modifier = Modifier
) {
    if (checkpoints.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada catatan riwayat dari kurir.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        checkpoints.forEachIndexed { index, checkpoint ->
            val isFirst = index == 0
            val isLast = index == checkpoints.size - 1

            TimelineItem(
                checkpoint = checkpoint,
                isLatest = isFirst,
                isLast = isLast
            )
        }
    }
}

@Composable
fun TimelineItem(
    checkpoint: Checkpoint,
    isLatest: Boolean,
    isLast: Boolean
) {
    val isDelivered = checkpoint.status == "DELIVERED" || checkpoint.description.contains("DITERIMA", ignoreCase = true)
    val dotColor = when {
        isDelivered -> Color(0xFF10B981)
        isLatest -> Color(0xFF2563EB)
        else -> Color(0xFF94A3B8)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .testTag("timeline_item")
    ) {
        // Left Column: Dot & vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isLatest) 16.dp else 12.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                if (isLatest) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFCBD5E1))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Column: Card with description, time, and location
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLatest) Color(0xFFF0F7FF) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isLatest) 3.dp else 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (isLatest) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = if (isDelivered) "STATUS AKHIR" else "STATUS TERKINI",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = checkpoint.description,
                        fontSize = 14.sp,
                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                        color = Color(0xFF0F172A),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = checkpoint.dateTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )

                        if (checkpoint.location.isNotBlank()) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Lokasi",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = checkpoint.location,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
