package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdManager
import com.example.ads.ui.InterstitialTriggerDialog
import com.example.notification.NotificationHelper
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PackageDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PackageViewModel

sealed class Screen {
    data object Home : Screen()
    data class Detail(val packageId: Long) : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: PackageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Initialize IronSource and Meta Audience Network mediation
        AdManager.initialize(this)

        setContent {
            MyApplicationTheme {
                // Request Notification Permission on Android 13+ (Tiramisu)
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                val showFallbackInterstitial by AdManager.showFallbackInterstitial.collectAsStateWithLifecycle()

                if (showFallbackInterstitial) {
                    InterstitialTriggerDialog(
                        onDismiss = { AdManager.dismissFallbackInterstitial() }
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onNavigateToDetail = { id ->
                                        currentScreen = Screen.Detail(id)
                                    },
                                    onNavigateToSettings = {
                                        currentScreen = Screen.Settings
                                    }
                                )
                            }
                            is Screen.Detail -> {
                                val selectedPackage = uiState.packages.find { it.id == screen.packageId }
                                PackageDetailScreen(
                                    packageEntity = selectedPackage,
                                    viewModel = viewModel,
                                    isLoading = uiState.isLoading,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                            is Screen.Settings -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.Home }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        AdManager.onPause(this)
    }
}
