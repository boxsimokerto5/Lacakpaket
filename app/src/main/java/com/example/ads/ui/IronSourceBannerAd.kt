package com.example.ads.ui

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdManager
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener

/**
 * IronSource & Meta Audience Network Banner Composable.
 * Renders a standard 50dp banner ad seamlessly in the view hierarchy
 * with automatic initialization synchronization and resilient retry logic.
 */
@Composable
fun IronSourceBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isAdLoaded by remember { mutableStateOf(false) }
    var bannerLayoutRef by remember { mutableStateOf<IronSourceBannerLayout?>(null) }
    var retryCount by remember { mutableIntStateOf(0) }
    val isSdkInitialized by AdManager.isInitialized.collectAsStateWithLifecycle()

    // Trigger load when SDK completes initialization if not yet loaded
    LaunchedEffect(isSdkInitialized, bannerLayoutRef) {
        val banner = bannerLayoutRef
        if (isSdkInitialized && banner != null && !isAdLoaded) {
            try {
                Log.d("IronSourceBanner", "SDK initialized, triggering banner load")
                IronSource.loadBanner(banner)
            } catch (e: Exception) {
                Log.w("IronSourceBanner", "Trigger loadBanner failed: ${e.message}")
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("ironsource_banner_container"),
        color = Color(0xFFF8FAFC),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Live Ad View container
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val act = (ctx as? Activity) ?: activity
                    if (act != null) {
                        try {
                            val banner = IronSource.createBanner(act, ISBannerSize.BANNER)
                            if (banner != null) {
                                bannerLayoutRef = banner
                                banner.setLevelPlayBannerListener(object : LevelPlayBannerListener {
                                    override fun onAdLoaded(adInfo: AdInfo) {
                                        Log.d("IronSourceBanner", "Banner loaded successfully: ${adInfo.adNetwork}")
                                        act.runOnUiThread {
                                            isAdLoaded = true
                                        }
                                    }

                                    override fun onAdLoadFailed(error: IronSourceError) {
                                        Log.w(
                                            "IronSourceBanner",
                                            "Banner load notice: ${error.errorMessage} (${error.errorCode})"
                                        )
                                        // Automatic graceful retry with backoff for reliable ad fill
                                        if (retryCount < 3) {
                                            retryCount++
                                            frameLayout.postDelayed({
                                                try {
                                                    Log.d(
                                                        "IronSourceBanner",
                                                        "Retrying banner load (attempt $retryCount)..."
                                                    )
                                                    IronSource.loadBanner(banner)
                                                } catch (e: Exception) {
                                                    Log.w("IronSourceBanner", "Retry load error: ${e.message}")
                                                }
                                            }, 2500L * retryCount)
                                        }
                                    }

                                    override fun onAdClicked(adInfo: AdInfo) {
                                        Log.d("IronSourceBanner", "Banner clicked: ${adInfo.adNetwork}")
                                    }

                                    override fun onAdLeftApplication(adInfo: AdInfo) {}

                                    override fun onAdScreenPresented(adInfo: AdInfo) {}

                                    override fun onAdScreenDismissed(adInfo: AdInfo) {}
                                })

                                frameLayout.addView(banner)

                                // Primary banner load attempt
                                try {
                                    IronSource.loadBanner(banner)
                                } catch (e: Exception) {
                                    Log.w("IronSourceBanner", "Initial loadBanner error: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("IronSourceBanner", "Error initializing banner view: ${e.message}", e)
                        }
                    }
                    frameLayout
                },
                onRelease = { container ->
                    for (i in 0 until container.childCount) {
                        val child = container.getChildAt(i)
                        if (child is IronSourceBannerLayout) {
                            try {
                                IronSource.destroyBanner(child)
                            } catch (e: Exception) {
                                Log.w("IronSourceBanner", "Error destroying banner: ${e.message}")
                            }
                        }
                    }
                    container.removeAllViews()
                    bannerLayoutRef = null
                }
            )

            // Sleek subtle placeholder visible smoothly before first ad fill arrives
            AnimatedVisibility(
                visible = !isAdLoaded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Iklan Sponsor • IronSource & Meta",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IKLAN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}
