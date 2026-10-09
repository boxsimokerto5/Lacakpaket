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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.model.CourierList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    // skipPartiallyExpanded = false allows bottom sheet to open at half-screen
    // and expand to 3/4 screen when swiped up, mimicking Google Maps
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_package_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header with Logo & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lacak Paket Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Mendukung 11+ kurir logistik Indonesia",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                    },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) {
                            Text(validationError ?: "", color = MaterialTheme.colorScheme.error)
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

                Spacer(modifier = Modifier.height(14.dp))

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
                        CourierBrandButton(
                            courier = courier,
                            isSelected = selectedCourierCode == courier.code,
                            onClick = { selectedCourierCode = courier.code }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom note / label
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Catatan Paket (Opsional)") },
                    placeholder = { Text("Misal: Belanjaan Toko, Kado, dll.") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("package_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_button")
                ) {
                    Text("Batal", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (waybill.isBlank()) {
                            validationError = "Silakan masukkan nomor resi terlebih dahulu"
                        } else {
                            onSubmit(waybill, selectedCourierCode, customTitle)
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("submit_track_button")
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
                        Text("Lacak Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
