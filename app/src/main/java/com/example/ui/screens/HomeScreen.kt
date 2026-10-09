package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource
import com.example.ads.AdManager
import com.example.ads.ui.IronSourceBannerAd
import com.example.ads.ui.NativeAdCard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TrackedPackageEntity
import com.example.data.model.CourierList
import com.example.ui.components.AddPackageDialog
import com.example.ui.components.CourierBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlueGradient
import com.example.ui.theme.CoralOrangeGradient
import com.example.ui.theme.HeaderCardGradient
import com.example.ui.theme.MintEmeraldGradient
import com.example.ui.viewmodel.PackageViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    viewModel: PackageViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            IronSourceBannerAd()
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.app_logo),
                            contentDescription = "Logo LacakPaket",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Lacak Paket",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Lacak Seluruh Ekspedisi Indonesia",
                                fontSize = 11.sp,
                                color = Color(0xFFC2410C),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    }
                },

                actions = {
                    IconButton(
                        onClick = {
                            activity?.let { AdManager.recordUserClick(it) }
                            viewModel.refreshAll()
                        },
                        enabled = !uiState.isRefreshingAll,
                        modifier = Modifier.testTag("refresh_all_button")
                    ) {
                        if (uiState.isRefreshingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Segarkan Semua",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
                text = { Text("Lacak Resi Baru", fontWeight = FontWeight.Bold, color = Color.White) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("add_package_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFD))
        ) {
            // Bright Colorful Stats Row
            ColorfulStatsBar(packages = uiState.packages)

            // Search Bar with clean bright style
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_package_input"),
                placeholder = { Text("Cari no. resi, barang, atau kurir...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Filter Chips with horizontal scroll to prevent squishing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val totalCount = uiState.packages.size
                val activeCount = uiState.packages.count { !it.isDelivered }
                val deliveredCount = uiState.packages.count { it.isDelivered }

                FilterChip(
                    selected = uiState.filterTab == "ALL",
                    onClick = {
                        activity?.let { AdManager.recordUserClick(it) }
                        viewModel.setFilterTab("ALL")
                    },
                    label = { Text("Semua ($totalCount)", fontWeight = FontWeight.SemiBold, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = uiState.filterTab == "ACTIVE",
                    onClick = {
                        activity?.let { AdManager.recordUserClick(it) }
                        viewModel.setFilterTab("ACTIVE")
                    },
                    label = { Text("Dalam Perjalanan ($activeCount)", fontWeight = FontWeight.SemiBold, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF97316),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_active")
                )
                FilterChip(
                    selected = uiState.filterTab == "DELIVERED",
                    onClick = {
                        activity?.let { AdManager.recordUserClick(it) }
                        viewModel.setFilterTab("DELIVERED")
                    },
                    label = { Text("Terkirim ($deliveredCount)", fontWeight = FontWeight.SemiBold, softWrap = false) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_delivered")
                )
            }


            // List of Packages
            if (uiState.filteredPackages.isEmpty()) {
                EmptyStateView(
                    isSearch = uiState.searchQuery.isNotBlank(),
                    onOpenAddDialog = {
                        activity?.let { AdManager.recordUserClick(it) }
                        showAddDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.filteredPackages,
                        key = { it.id }
                    ) { pkg ->
                        BrightPackageCardItem(
                            pkg = pkg,
                            onClick = {
                                activity?.let { AdManager.recordUserClick(it) }
                                onNavigateToDetail(pkg.id)
                            },
                            onCopyWaybill = {
                                activity?.let { AdManager.recordUserClick(it) }
                                clipboardManager.setText(AnnotatedString(pkg.waybill))
                                Toast.makeText(context, "Resi ${pkg.waybill} disalin!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Native Ad integrated into package feed
                    item {
                        NativeAdCard(
                            modifier = Modifier.padding(vertical = 4.dp),
                            onAdClicked = {
                                activity?.let { AdManager.recordUserClick(it) }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPackageDialog(
            isLoading = uiState.isLoading,
            onDismiss = { showAddDialog = false },
            onSubmit = { waybill, courierCode, customTitle ->
                activity?.let { AdManager.recordUserClick(it) }
                viewModel.trackAndSavePackage(waybill, courierCode, customTitle) { success, _ ->
                    if (success) {
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun ColorfulStatsBar(packages: List<TrackedPackageEntity>) {
    val total = packages.size
    val active = packages.count { !it.isDelivered }
    val delivered = packages.count { it.isDelivered }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GradientStatCard(
            title = "Total Resi",
            count = total.toString(),
            gradient = BlueGradient,
            icon = Icons.Default.Inventory2,
            modifier = Modifier.weight(1f)
        )
        GradientStatCard(
            title = "Bergerak",
            count = active.toString(),
            gradient = CoralOrangeGradient,
            icon = Icons.Default.LocalShipping,
            modifier = Modifier.weight(1f)
        )
        GradientStatCard(
            title = "Tiba",
            count = delivered.toString(),
            gradient = MintEmeraldGradient,
            icon = Icons.Default.TaskAlt,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GradientStatCard(
    title: String,
    count: String,
    gradient: Brush,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = count,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun BrightPackageCardItem(
    pkg: TrackedPackageEntity,
    onClick: () -> Unit,
    onCopyWaybill: () -> Unit
) {
    val courier = remember(pkg.courierCode) { CourierList.findByCode(pkg.courierCode) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("id-ID")) }
    val lastUpdateText = remember(pkg.lastUpdated) { dateFormat.format(Date(pkg.lastUpdated)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("package_card_${pkg.waybill}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Colorful left accent bar using courier brand color
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(courier.brandColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Row 1: Courier Badge + Courier Name on left, Status Badge on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        CourierBadge(courierCode = pkg.courierCode)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = courier.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    StatusBadge(isDelivered = pkg.isDelivered)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Waybill with Copy Button (full line, never squished!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pkg.waybill,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A),
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onCopyWaybill,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Nomor Resi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Custom Package Title if present
                if (pkg.customTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pkg.customTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Origin -> Destination with defensive asterisk sanitization
                val cleanOrigin = remember(pkg.origin) {
                    com.example.data.remote.TrackingApiBridge.sanitizeMaskedText(pkg.origin)
                }
                val cleanDestination = remember(pkg.destination) {
                    com.example.data.remote.TrackingApiBridge.sanitizeMaskedText(pkg.destination)
                }

                if (cleanOrigin.isNotBlank() && cleanDestination.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = cleanOrigin,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cleanDestination,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                } else if (cleanDestination.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tujuan: $cleanDestination",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (cleanOrigin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Asal: $cleanOrigin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Latest Checkpoint Banner
                val enrichedHomeDesc = remember(pkg) {
                    com.example.data.remote.TrackingApiBridge.enrichStatusDescription(
                        rawNote = pkg.statusDescription,
                        location = "",
                        isFirst = true,
                        isLast = false,
                        isDelivered = pkg.isDelivered
                    )
                }
                Surface(
                    color = if (pkg.isDelivered) Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = enrichedHomeDesc,
                        fontSize = 12.sp,
                        color = if (pkg.isDelivered) Color(0xFF166534) else Color(0xFF1E40AF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Diperbarui: $lastUpdateText",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyStateView(
    isSearch: Boolean,
    onOpenAddDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = com.example.R.drawable.app_logo),
                contentDescription = "Logo Lacak Paket",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isSearch) "Tidak Ada Resi yang Cocok" else "Belum Ada Paket Dilacak",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSearch) {
                    "Coba periksa kembali ejaan nomor resi atau catatan paket Anda."
                } else {
                    "Lacak status paket secara akurat dan real-time langsung dari server resmi ekspedisi kurir logistik."
                },
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )

            if (!isSearch) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onOpenAddDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("empty_add_package_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lacak Resi Baru",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Native Ad preview
            NativeAdCard(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
