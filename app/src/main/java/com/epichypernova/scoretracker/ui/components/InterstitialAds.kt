package com.epichypernova.scoretracker.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.epichypernova.scoretracker.BuildConfig
import com.epichypernova.scoretracker.data.AppActions
import com.epichypernova.scoretracker.data.Repository
import com.epichypernova.scoretracker.data.model.AppState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Interstitial ad. Ad unit id comes from BuildConfig (test in debug, admob.properties in release).
 * [onDone] always runs (after dismissal, or immediately on any failure / before consent is gathered).
 */
fun showInterstitialAd(context: Context, adUnitId: String = BuildConfig.ADMOB_INTERSTITIAL, onDone: () -> Unit) {
    val activity = context.findActivity()
    if (activity == null || !AdsConsent.canRequestAds.value) { onDone(); return }
    InterstitialAd.load(
        context,
        adUnitId,
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) = onDone()
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() = onDone()
                    override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) = onDone()
                }
                ad.show(activity)
            }
        },
    )
}

/**
 * Returns a gate: call it with the game-start action. Every 7 finished games it shows an
 * interstitial first, then runs the action; otherwise it runs the action immediately.
 */
@Composable
fun rememberAdGate(repo: Repository, state: AppState): (() -> Unit) -> Unit {
    val context = LocalContext.current
    return { proceed ->
        if (AppActions.adDue(state)) {
            repo.update { AppActions.markAdShown(it) }
            showInterstitialAd(context) { proceed() }
        } else {
            proceed()
        }
    }
}
