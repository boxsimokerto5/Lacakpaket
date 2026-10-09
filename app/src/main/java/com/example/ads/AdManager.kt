package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdManager handles initialization, mediation, and ad formats for
 * ironSource SDK and Meta Audience Network (Facebook Adapter).
 *
 * It manages:
 * 1. Banner ads
 * 2. Native ads
 * 3. Interstitial ads with an 8-click threshold
 */
object AdManager {
    private const val TAG = "AdManager"
    private const val PREFS_NAME = "ad_preferences"
    private const val KEY_APP_KEY = "ironsource_app_key"

    // Default IronSource test app key (LevelPlay official demo key)
    const val DEFAULT_DEMO_APP_KEY = "854609f3"
    const val CLICKS_PER_INTERSTITIAL = 8

    private val _clickCount = MutableStateFlow(0)
    val clickCount: StateFlow<Int> = _clickCount.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    private val _showFallbackInterstitial = MutableStateFlow(false)
    val showFallbackInterstitial: StateFlow<Boolean> = _showFallbackInterstitial.asStateFlow()

    private var isInitialized = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getIronSourceAppKey(context: Context): String {
        return getPrefs(context).getString(KEY_APP_KEY, DEFAULT_DEMO_APP_KEY) ?: DEFAULT_DEMO_APP_KEY
    }

    fun setIronSourceAppKey(context: Context, appKey: String) {
        getPrefs(context).edit().putString(KEY_APP_KEY, appKey.trim()).apply()
    }

    /**
     * Initializes IronSource and Meta Audience Network adapter.
     */
    fun initialize(activity: Activity) {
        val appKey = getIronSourceAppKey(activity)

        try {
            // 1. Configure Meta Audience Network adapter settings via IronSource metadata
            IronSource.setMetaData("Facebook_IS_reporting", "true")
            IronSource.setMetaData("is_child_directed", "false")
            IronSource.setConsent(true)

            // 2. Initialize Meta Audience Network SDK directly for optimum mediation readiness
            AudienceNetworkAds.initialize(activity)
            Log.d(TAG, "Meta Audience Network SDK initialized successfully")

            // 3. Set LevelPlay Interstitial Listener
            IronSource.setLevelPlayInterstitialListener(object : LevelPlayInterstitialListener {
                override fun onAdReady(adInfo: AdInfo) {
                    Log.d(TAG, "IronSource Interstitial Ready: ${adInfo.adNetwork}")
                    _isInterstitialReady.value = true
                }

                override fun onAdLoadFailed(error: IronSourceError) {
                    Log.w(TAG, "IronSource Interstitial Load Failed: ${error.errorMessage} (${error.errorCode})")
                    _isInterstitialReady.value = false
                }

                override fun onAdOpened(adInfo: AdInfo) {
                    Log.d(TAG, "IronSource Interstitial Opened")
                }

                override fun onAdShowSucceeded(adInfo: AdInfo) {
                    Log.d(TAG, "IronSource Interstitial Show Succeeded")
                    _isInterstitialReady.value = false
                }

                override fun onAdShowFailed(error: IronSourceError, adInfo: AdInfo) {
                    Log.e(TAG, "IronSource Interstitial Show Failed: ${error.errorMessage}")
                    _isInterstitialReady.value = false
                    // Pre-load next
                    loadInterstitial()
                }

                override fun onAdClicked(adInfo: AdInfo) {
                    Log.d(TAG, "IronSource Interstitial Clicked")
                }

                override fun onAdClosed(adInfo: AdInfo) {
                    Log.d(TAG, "IronSource Interstitial Closed. Loading next.")
                    _isInterstitialReady.value = false
                    loadInterstitial()
                }
            })

            // 4. Initialize IronSource Mediation SDK with Interstitial & Banner units
            IronSource.init(
                activity,
                appKey,
                IronSource.AD_UNIT.INTERSTITIAL,
                IronSource.AD_UNIT.BANNER
            )

            isInitialized = true
            Log.d(TAG, "IronSource initialized with App Key: $appKey")

            // Initial load of interstitial
            loadInterstitial()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdManager: ${e.message}", e)
        }
    }

    fun onResume(activity: Activity) {
        try {
            IronSource.onResume(activity)
        } catch (e: Exception) {
            Log.w(TAG, "onResume error: ${e.message}")
        }
    }

    fun onPause(activity: Activity) {
        try {
            IronSource.onPause(activity)
        } catch (e: Exception) {
            Log.w(TAG, "onPause error: ${e.message}")
        }
    }

    fun loadInterstitial() {
        try {
            IronSource.loadInterstitial()
        } catch (e: Exception) {
            Log.w(TAG, "loadInterstitial error: ${e.message}")
        }
    }

    /**
     * Increments user interactive clicks.
     * When reaching 8 clicks, displays the Interstitial ad!
     */
    fun recordUserClick(activity: Activity) {
        val current = _clickCount.value + 1
        Log.d(TAG, "User click recorded: $current / $CLICKS_PER_INTERSTITIAL")

        if (current >= CLICKS_PER_INTERSTITIAL) {
            _clickCount.value = 0
            showInterstitial(activity)
        } else {
            _clickCount.value = current
        }
    }

    /**
     * Shows Interstitial Ad if available, or fallback dialog if ad is not loaded.
     */
    fun showInterstitial(activity: Activity) {
        try {
            if (IronSource.isInterstitialReady()) {
                Log.d(TAG, "Showing IronSource live Interstitial")
                IronSource.showInterstitial("DefaultPlacement")
            } else {
                Log.d(TAG, "Interstitial not ready yet. Showing notification fallback & reloading.")
                _showFallbackInterstitial.value = true
                loadInterstitial()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show interstitial: ${e.message}", e)
            _showFallbackInterstitial.value = true
        }
    }

    fun dismissFallbackInterstitial() {
        _showFallbackInterstitial.value = false
    }

    /**
     * Helper to create Banner
     */
    fun createBanner(activity: Activity): IronSourceBannerLayout? {
        return try {
            IronSource.createBanner(activity, ISBannerSize.BANNER)
        } catch (e: Exception) {
            Log.e(TAG, "createBanner error: ${e.message}", e)
            null
        }
    }

    fun destroyBanner(banner: IronSourceBannerLayout?) {
        if (banner != null) {
            try {
                IronSource.destroyBanner(banner)
            } catch (e: Exception) {
                Log.w(TAG, "destroyBanner error: ${e.message}")
            }
        }
    }
}
