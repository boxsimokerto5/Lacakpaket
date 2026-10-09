package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.TrackedPackageEntity
import com.example.data.model.CourierList
import com.example.data.repository.PackageRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val packages: List<TrackedPackageEntity> = emptyList(),
    val filteredPackages: List<TrackedPackageEntity> = emptyList(),
    val filterTab: String = "ALL", // "ALL", "ACTIVE", "DELIVERED"
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshingAll: Boolean = false,
    val userMessage: String? = null,
    val apiKey: String = "",
    val isNotificationEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 30
)

data class ExtraState(
    val isRefreshingAll: Boolean = false,
    val userMessage: String? = null,
    val apiKey: String = "",
    val isNotificationEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 30
)

class PackageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PackageRepository(application)

    private val _filterTab = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _extraState = MutableStateFlow(
        ExtraState(
            apiKey = repository.getApiKey(),
            isNotificationEnabled = repository.isNotificationEnabled(),
            syncIntervalMinutes = 30
        )
    )

    init {
        // Automatic background checker every 30 minutes
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30L * 60 * 1000L) // 30 minutes
                if (_extraState.value.isNotificationEnabled) {
                    try {
                        repository.checkAllActivePackages()
                    } catch (_: Exception) {
                        // Silent in background
                    }
                }
            }
        }
    }

    val uiState: StateFlow<UiState> = combine(
        repository.allPackages,
        _filterTab,
        _searchQuery,
        _isLoading,
        _extraState
    ) { packages, filter, query, loading, extra ->
        val filtered = packages.filter { pkg ->
            val matchFilter = when (filter) {
                "ACTIVE" -> !pkg.isDelivered
                "DELIVERED" -> pkg.isDelivered
                else -> true
            }
            val matchQuery = if (query.isBlank()) {
                true
            } else {
                pkg.waybill.contains(query, ignoreCase = true) ||
                        pkg.courierName.contains(query, ignoreCase = true) ||
                        pkg.customTitle.contains(query, ignoreCase = true) ||
                        pkg.statusDescription.contains(query, ignoreCase = true)
            }
            matchFilter && matchQuery
        }

        UiState(
            packages = packages,
            filteredPackages = filtered,
            filterTab = filter,
            searchQuery = query,
            isLoading = loading,
            isRefreshingAll = extra.isRefreshingAll,
            userMessage = extra.userMessage,
            apiKey = extra.apiKey,
            isNotificationEnabled = extra.isNotificationEnabled,
            syncIntervalMinutes = extra.syncIntervalMinutes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun setFilterTab(tab: String) {
        _filterTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _extraState.update { it.copy(userMessage = null) }
    }

    fun trackAndSavePackage(
        waybill: String,
        courierCode: String,
        customTitle: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (waybill.isBlank()) {
            onComplete(false, "Nomor resi tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val courier = CourierList.findByCode(courierCode)
                val result = repository.fetchTrackingInfo(waybill.trim(), courier.code, customTitle.trim())

                if (!result.success) {
                    val errMsg = result.message.ifBlank {
                        "Nomor resi ${waybill.trim()} tidak ditemukan di sistem ${courier.name}. Pastikan nomor resi dan kurir yang dipilih sesuai data asli."
                    }
                    _extraState.update { it.copy(userMessage = errMsg) }
                    onComplete(false, errMsg)
                    return@launch
                }

                val entity = TrackedPackageEntity(
                    waybill = result.waybill,
                    courierCode = result.courierCode,
                    courierName = result.courierName,
                    customTitle = customTitle.trim(),
                    status = result.status,
                    statusDescription = result.statusDescription,
                    isDelivered = result.isDelivered,
                    origin = result.origin,
                    destination = result.destination,
                    shipper = result.shipper,
                    receiver = "",
                    lastUpdated = System.currentTimeMillis(),
                    lastNotifiedCheckpoint = result.statusDescription,
                    checkpointsJson = TrackedPackageEntity.checkpointsToJson(result.checkpoints)
                )

                repository.savePackage(entity)

                _extraState.update { it.copy(userMessage = "Resi ${result.waybill} (${courier.name}) berhasil dilacak") }
                onComplete(true, "Berhasil melacak resi asli")
            } catch (e: Exception) {
                val errMsg = "Gagal melacak: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                _extraState.update { it.copy(userMessage = errMsg) }
                onComplete(false, errMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPackage(pkg: TrackedPackageEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.fetchTrackingInfo(pkg.waybill, pkg.courierCode, pkg.customTitle)
                if (result.success) {
                    val updated = pkg.copy(
                        status = result.status,
                        statusDescription = result.statusDescription,
                        isDelivered = result.isDelivered,
                        origin = result.origin.ifBlank { pkg.origin },
                        destination = result.destination.ifBlank { pkg.destination },
                        shipper = result.shipper.ifBlank { pkg.shipper },
                        receiver = "",
                        lastUpdated = System.currentTimeMillis(),
                        lastNotifiedCheckpoint = result.statusDescription,
                        checkpointsJson = TrackedPackageEntity.checkpointsToJson(result.checkpoints)
                    )
                    repository.updatePackage(updated)
                    _extraState.update { it.copy(userMessage = "Status resi ${pkg.waybill} berhasil diperbarui") }
                } else {
                    _extraState.update { it.copy(userMessage = result.message.ifBlank { "Belum ada pembaruan untuk resi ${pkg.waybill}" }) }
                }
            } catch (e: Exception) {
                _extraState.update { it.copy(userMessage = "Gagal memperbarui: ${e.message}") }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _extraState.update { it.copy(isRefreshingAll = true) }
            try {
                val count = repository.checkAllActivePackages()
                val msg = if (count > 0) {
                    "$count paket aktif mendapatkan pembaruan status!"
                } else {
                    "Semua resi sudah dalam status terbaru"
                }
                _extraState.update { it.copy(userMessage = msg) }
            } catch (e: Exception) {
                _extraState.update { it.copy(userMessage = "Gagal menyegarkan: ${e.message}") }
            } finally {
                _extraState.update { it.copy(isRefreshingAll = false) }
            }
        }
    }

    fun deletePackage(packageId: Long) {
        viewModelScope.launch {
            repository.deletePackage(packageId)
            _extraState.update { it.copy(userMessage = "Resi berhasil dihapus") }
        }
    }

    fun saveApiKey(newKey: String) {
        repository.setApiKey(newKey)
        _extraState.update { it.copy(apiKey = newKey, userMessage = "API Key berhasil disimpan") }
    }

    fun toggleNotification(enabled: Boolean) {
        repository.setNotificationEnabled(enabled)
        _extraState.update { it.copy(isNotificationEnabled = enabled) }
    }

    fun setSyncInterval(minutes: Int) {
        repository.setSyncIntervalMinutes(minutes)
        _extraState.update { it.copy(syncIntervalMinutes = minutes) }
    }

    fun sendTestNotification() {
        NotificationHelper.sendTestNotification(getApplication())
        _extraState.update { it.copy(userMessage = "Notifikasi uji coba telah dikirim!") }
    }
}
