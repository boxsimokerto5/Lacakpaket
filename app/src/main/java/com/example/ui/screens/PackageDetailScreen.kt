package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TrackedPackageEntity
import com.example.data.model.CourierList
import com.example.ui.components.CourierBadge
import com.example.ui.components.DeliveryProgressStepper
import com.example.ui.components.StatusBadge
import com.example.ui.components.TimelineView
import com.example.ui.theme.CoralOrangeGradient
import com.example.ui.viewmodel.PackageViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    packageEntity: TrackedPackageEntity?,
    viewModel: PackageViewModel,
    isLoading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (packageEntity == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Paket", color = Color(0xFF0F172A)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Paket tidak ditemukan")
            }
        }
        return
    }

    val courier = remember(packageEntity.courierCode) {
        CourierList.findByCode(packageEntity.courierCode)
    }
    val checkpoints = remember(packageEntity.checkpointsJson) {
        packageEntity.parseCheckpoints()
    }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")) }
    val lastUpdateStr = remember(packageEntity.lastUpdated) {
        dateFormat.format(Date(packageEntity.lastUpdated))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("package_detail_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = packageEntity.customTitle.ifBlank { packageEntity.waybill },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    // Share Button
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                append("📦 Status Paket ${courier.name}\n")
                                append("No. Resi: ${packageEntity.waybill}\n")
                                if (packageEntity.customTitle.isNotBlank()) {
                                    append("Nama: ${packageEntity.customTitle}\n")
                                }
                                append("Status: ${packageEntity.statusDescription}\n")
                                append("Terakhir update: $lastUpdateStr\n\n")
                                append("Dilacak via LacakPaket Android")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Bagikan Status Paket"))
                        },
                        modifier = Modifier.testTag("share_package_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshPackage(packageEntity) },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("detail_refresh_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Delete Button
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color(0xFFDC2626)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F8FC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Courier & Waybill Radiant Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("courier_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CourierBadge(courierCode = packageEntity.courierCode)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = courier.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        StatusBadge(isDelivered = packageEntity.isDelivered)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Waybill with highlighted copy container
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOMOR RESI PENGIRIMAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = packageEntity.waybill,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 1.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(packageEntity.waybill))
                                    Toast.makeText(context, "Resi ${packageEntity.waybill} disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .testTag("copy_waybill_detail")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin Nomor Resi",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Terakhir diperbarui: $lastUpdateStr",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Progress Stepper with bright milestone markers
            DeliveryProgressStepper(
                isDelivered = packageEntity.isDelivered,
                checkpointsCount = checkpoints.size
            )

            // Shipping Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DETAIL PENGIRIMAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Rute Perjalanan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${packageEntity.origin.ifBlank { "Jakarta" }} → ${packageEntity.destination.ifBlank { "Tujuan" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Penerima",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = packageEntity.receiver.ifBlank { packageEntity.customTitle.ifBlank { "Penerima Paket" } },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            // Real-time Simulation Action Trigger Card with warm radiant gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulation_trigger_card"),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(CoralOrangeGradient)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Uji Coba Notifikasi Real-Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tekan tombol di bawah untuk memajukan status pengiriman paket dan seketika membunyikan notifikasi sistem Android.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.advanceSimulation(packageEntity.id) },
                            enabled = !packageEntity.isDelivered && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFFC2410C),
                                disabledContainerColor = Color.White.copy(alpha = 0.6f),
                                disabledContentColor = Color(0xFF78350F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_simulation_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (packageEntity.isDelivered) "Paket Sudah Tiba (Selesai)" else "Simulasi Majukan Status & Kirim Notifikasi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Timeline Header
            Text(
                text = "RIWAYAT CHECKPOINT PERJALANAN",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Vertical Timeline View
            TimelineView(checkpoints = checkpoints)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Resi Ini?", fontWeight = FontWeight.Bold) },
            text = { Text("Resi ${packageEntity.waybill} (${courier.name}) akan dihapus dari daftar pantauan Anda.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePackage(packageEntity.id)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
