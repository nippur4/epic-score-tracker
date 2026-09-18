package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.epichypernova.scoretracker.BuildConfig
import com.epichypernova.scoretracker.ui.theme.Palette
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Anchored AdMob banner. Reserves the banner height from the start and only creates the AdView
 * once [AdsConsent.canRequestAds] is true, so the layout never jumps when consent resolves.
 * Ad unit id comes from BuildConfig (test in debug, admob.properties in release).
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier, adUnitId: String = BuildConfig.ADMOB_BANNER) {
    val canRequestAds by AdsConsent.canRequestAds.collectAsState()
    Box(
        modifier = modifier.fillMaxWidth().height(AdSize.BANNER.height.dp).background(Palette.AppBgDeep),
        contentAlignment = Alignment.Center,
    ) {
        if (canRequestAds) {
            AndroidView(
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        setAdUnitId(adUnitId)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
