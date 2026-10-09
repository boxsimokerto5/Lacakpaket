package com.example.update

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * InAppUpdateHelper provides official Google Play In-App Updates support.
 * When a newer version of the application is published to Google Play Store,
 * it automatically detects the release and presents the official Google Play
 * update dialog to the user (both Flexible and Immediate update flows).
 */
object InAppUpdateHelper {
    private const val TAG = "InAppUpdateHelper"
    const val REQUEST_CODE_FLEXIBLE_UPDATE = 5510
    const val REQUEST_CODE_IMMEDIATE_UPDATE = 5511

    private var appUpdateManager: AppUpdateManager? = null

    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    private val _availableVersionCode = MutableStateFlow(0)
    val availableVersionCode: StateFlow<Int> = _availableVersionCode.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded.asStateFlow()

    private val _statusMessage = MutableStateFlow("Versi Aplikasi Resmi Play Store")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = state.bytesDownloaded()
                val totalBytes = state.totalBytesToDownload()
                val percent = if (totalBytes > 0) (bytesDownloaded * 100 / totalBytes).toInt() else 0
                _isDownloading.value = true
                _statusMessage.value = "Mengunduh pembaruan Play Store ($percent%)..."
                Log.d(TAG, "Update downloading: $percent%")
            }
            InstallStatus.DOWNLOADED -> {
                _isDownloading.value = false
                _isDownloaded.value = true
                _statusMessage.value = "Pembaruan siap dipasang! Sentuh untuk mulai."
                Log.d(TAG, "Update downloaded and ready to install")
            }
            InstallStatus.FAILED -> {
                _isDownloading.value = false
                _statusMessage.value = "Gagal mengunduh pembaruan Play Store"
                Log.w(TAG, "Update failed: ${state.installErrorCode()}")
            }
            InstallStatus.CANCELED -> {
                _isDownloading.value = false
                _statusMessage.value = "Pembaruan dibatalkan pengguna"
            }
            else -> {}
        }
    }

    fun init(context: Context) {
        if (appUpdateManager == null) {
            val manager = AppUpdateManagerFactory.create(context.applicationContext)
            appUpdateManager = manager
            manager.registerListener(installStateUpdatedListener)
        }
    }

    /**
     * Checks Google Play Store for new version updates.
     * When immediate=true, prompts the official Google Play full-screen immediate update dialog.
     * When immediate=false, supports flexible in-app background update.
     */
    fun checkForUpdates(
        activity: Activity,
        immediate: Boolean = false,
        onResult: ((hasUpdate: Boolean, message: String) -> Unit)? = null
    ) {
        val manager = appUpdateManager ?: AppUpdateManagerFactory.create(activity.applicationContext).also {
            appUpdateManager = it
            it.registerListener(installStateUpdatedListener)
        }

        manager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            val availableVersion = appUpdateInfo.availableVersionCode()
            _availableVersionCode.value = availableVersion

            if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                _isUpdateAvailable.value = true
                val mode = if (immediate && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    AppUpdateType.FLEXIBLE
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else {
                    -1
                }

                if (mode != -1) {
                    val modeName = if (mode == AppUpdateType.IMMEDIATE) "Immediate" else "Flexible"
                    _statusMessage.value = "Pembaruan v$availableVersion tersedia di Google Play!"
                    Log.d(TAG, "Update available! Starting $modeName flow for version $availableVersion")
                    onResult?.invoke(true, "Versi baru v$availableVersion tersedia di Play Store!")

                    try {
                        val requestCode = if (mode == AppUpdateType.IMMEDIATE) REQUEST_CODE_IMMEDIATE_UPDATE else REQUEST_CODE_FLEXIBLE_UPDATE
                        @Suppress("DEPRECATION")
                        manager.startUpdateFlowForResult(appUpdateInfo, mode, activity, requestCode)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start update flow: ${e.message}", e)
                    }
                } else {
                    _statusMessage.value = "Pembaruan tersedia di Play Store"
                    onResult?.invoke(true, "Pembaruan tersedia di Play Store")
                }
            } else if (availability == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.d(TAG, "Resuming in-progress update")
                try {
                    @Suppress("DEPRECATION")
                    manager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        REQUEST_CODE_IMMEDIATE_UPDATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed resuming update: ${e.message}", e)
                }
            } else {
                _isUpdateAvailable.value = false
                _statusMessage.value = "Aplikasi Anda sudah versi terbaru (Google Play Store)"
                onResult?.invoke(false, "Aplikasi Anda sudah versi paling mutakhir!")
            }
        }.addOnFailureListener { error ->
            Log.w(TAG, "Check update failed: ${error.message}")
            _statusMessage.value = "Versi aplikasi saat ini aktif"
            onResult?.invoke(false, "Pemeriksaan Play Store selesai. Versi aplikasi saat ini aktif.")
        }
    }

    /**
     * Resumes check when app returns to foreground.
     */
    fun onResume(activity: Activity) {
        val manager = appUpdateManager ?: return
        manager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                _isDownloaded.value = true
                _statusMessage.value = "Pembaruan berhasil diunduh. Siap dipasang!"
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    @Suppress("DEPRECATION")
                    manager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        REQUEST_CODE_IMMEDIATE_UPDATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed resuming update on resume: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Installs flexible update and restarts the app.
     */
    fun completeUpdate() {
        appUpdateManager?.completeUpdate()
    }

    fun onDestroy() {
        appUpdateManager?.unregisterListener(installStateUpdatedListener)
    }
}
