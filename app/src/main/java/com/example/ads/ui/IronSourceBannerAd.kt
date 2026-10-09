package com.example.ads.ui

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.example.ads.AdManager
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener

/**
 * IronSource & Meta Audience Network Banner Composable.
 * Embeds the real ironSource banner view with graceful fallback.
 */
@Composable
fun IronSourceBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isAdLoaded by remember { mutableStateOf(false) }
    var bannerLayout by remember { mutableStateOf<IronSourceBannerLayout?>(null) }

    DisposableEffect(activity) {
        if (activity != null) {
            try {
                val layout = IronSource.createBanner(activity, ISBannerSize.BANNER)
                bannerLayout = layout

                layout?.setLevelPlayBannerListener(object : LevelPlayBannerListener {
                    override fun onAdLoaded(adInfo: AdInfo) {
                        Log.d("IronSourceBanner", "Banner loaded from network: ${adInfo.adNetwork}")
                        isAdLoaded = true
                    }

                    override fun onAdLoadFailed(error: IronSourceError) {
                        Log.w("IronSourceBanner", "Banner load failed: ${error.errorMessage} (${error.errorCode})")
                        isAdLoaded = false
                    }

                    override fun onAdClicked(adInfo: AdInfo) {
                        Log.d("IronSourceBanner", "Banner clicked")
                    }

                    override fun onAdLeftApplication(adInfo: AdInfo) {}

                    override fun onAdScreenPresented(adInfo: AdInfo) {}

                    override fun onAdScreenDismissed(adInfo: AdInfo) {}
                })

                if (layout != null) {
                    IronSource.loadBanner(layout)
                }
            } catch (e: Exception) {
                Log.e("IronSourceBanner", "Error initiating banner: ${e.message}")
            }
        }

        onDispose {
            bannerLayout?.let {
                try {
                    IronSource.destroyBanner(it)
                } catch (e: Exception) {
                    Log.w("IronSourceBanner", "Error destroying banner: ${e.message}")
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ironsource_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        if (isAdLoaded && bannerLayout != null) {
            AndroidView(
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    bannerLayout?.let { bl ->
                        (bl.parent as? ViewGroup)?.removeView(bl)
                        frameLayout.addView(bl)
                    }
                    frameLayout
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        } else {
            // Elegant placeholder card indicating Banner Ad presence while waiting for fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF4338CA),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IKLAN BERSAMPUR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFE2E8F0))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IronSource & Meta Audience",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }
                        Text(
                            text = "Banner Aktif • Menampilkan iklan sponsor yang relevan",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
