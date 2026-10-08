package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CourierList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPackageDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (waybill: String, courierCode: String, customTitle: String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var waybill by remember { mutableStateOf("") }
    var selectedCourierCode by remember { mutableStateOf("jne") }
    var customTitle by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val detectedCourier = remember(waybill) {
        if (waybill.length >= 3) CourierList.detectCourier(waybill) else null
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .testTag("add_package_dialog"),

        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lacak Paket Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Waybill field with Paste button
                OutlinedTextField(
                    value = waybill,
                    onValueChange = {
                        waybill = it.trim().uppercase()
                        validationError = null
                        val detected = CourierList.detectCourier(it)
                        if (detected != null) {
                            selectedCourierCode = detected.code
                        }
                    },
                    label = { Text("Nomor Resi / AWB") },
                    placeholder = { Text("Contoh: JP123456789") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("waybill_input"),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        waybill = clip.trim().uppercase()
                                        val detected = CourierList.detectCourier(clip)
                                        if (detected != null) {
                                            selectedCourierCode = detected.code
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("paste_clipboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Tempel dari Clipboard",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) {
                            Text(validationError ?: "", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Mendukung 11+ kurir logistik Indonesia")
                        }
                    }
                )

                // Auto-detection badge
                if (detectedCourier != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCourierCode = detectedCourier.code }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Format cocok dengan: ${detectedCourier.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Courier selection
                Text(
                    text = "PILIH EKSPEDISI KURIR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourierList.supportedCouriers.forEach { courier ->
                        val isSelected = selectedCourierCode == courier.code
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) courier.brandColor else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) courier.brandColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCourierCode = courier.code }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("courier_select_${courier.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = courier.shortName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom title / label
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Nama Barang / Catatan (Opsional)") },
                    placeholder = { Text("Misal: Sepatu, Kado, atau Buku") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("package_title_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (waybill.isBlank()) {
                        validationError = "Silakan masukkan nomor resi terlebih dahulu"
                    } else {
                        onSubmit(waybill, selectedCourierCode, customTitle)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.testTag("submit_track_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Melacak...")
                } else {
                    Text("Lacak Sekarang")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier.testTag("cancel_button")
            ) {
                Text("Batal")
            }
        }
    )
}
