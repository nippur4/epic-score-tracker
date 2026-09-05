package com.epichypernova.scoretracker.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Loads and shows a rewarded ad. On reward, [onReward] runs. TEST ad unit — replace [TEST_REWARDED]
 * with your real ca-app-pub-XXXX/YYYY rewarded unit before publishing.
 */
private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

fun showRewardedAd(
    context: Context,
    onReward: () -> Unit,
    onError: (String) -> Unit = {},
    adUnitId: String = TEST_REWARDED,
) {
    val activity = context.findActivity()
    if (activity == null) { onError("No activity"); return }
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
