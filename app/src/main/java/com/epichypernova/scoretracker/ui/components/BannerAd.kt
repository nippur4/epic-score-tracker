package com.epichypernova.scoretracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.epichypernova.scoretracker.ui.theme.Palette
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Anchored AdMob banner. Uses Google's TEST banner unit id — replace [TEST_BANNER] with your real
 * ca-app-pub-XXXX/YYYY ad unit id before publishing (and the App ID in the manifest).
 */
private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun BannerAd(modifier: Modifier = Modifier, adUnitId: String = TEST_BANNER) {
    Box(
        modifier = modifier.fillMaxWidth().wrapContentHeight().background(Palette.AppBgDeep),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    setAdUnitId(adUnitId)
                    loadAd(AdRequest.Builder().build())
                }
            },
        )
    }
}
