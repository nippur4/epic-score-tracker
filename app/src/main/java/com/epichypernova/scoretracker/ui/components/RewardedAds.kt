package com.epichypernova.scoretracker.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.epichypernova.scoretracker.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Loads and shows a rewarded ad. On reward, [onReward] runs. Ad unit id comes from BuildConfig
 * (test in debug, admob.properties in release).
 */
fun showRewardedAd(
    context: Context,
    onReward: () -> Unit,
    onError: (String) -> Unit = {},
    adUnitId: String = BuildConfig.ADMOB_REWARDED,
) {
    val activity = context.findActivity()
    if (activity == null) { onError("No activity"); return }
    if (!AdsConsent.canRequestAds.value) { onError("Ads not available yet"); return }
    RewardedAd.load(
        context,
        adUnitId,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onError(error.message)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                ad.show(activity, OnUserEarnedRewardListener { onReward() })
            }
        },
    )
}

internal fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
