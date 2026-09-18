package com.epichypernova.scoretracker.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.epichypernova.scoretracker.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google UMP consent flow. Call [gather] from the launcher Activity's onCreate: it refreshes the
 * consent state, shows the GDPR/UK form when the user's region requires it, and only then
 * initializes the Mobile Ads SDK. Ad components observe [canRequestAds] and stay idle until it's true.
 *
 * The form is configured server-side in AdMob > Privacy & messaging (a "GDPR" message must be
 * published there, otherwise users in the EEA/UK get no ads at all).
 */
object AdsConsent {
    private const val TAG = "AdsConsent"

    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds

    private val sdkInitialized = AtomicBoolean(false)

    fun gather(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .apply { if (BuildConfig.DEBUG) setConsentDebugSettings(debugSettings(activity)) }
            .build()

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) Log.w(TAG, "Consent form: ${formError.errorCode} ${formError.message}")
                    if (info.canRequestAds()) initSdk(activity)
                }
            },
            { requestError ->
                Log.w(TAG, "Consent update: ${requestError.errorCode} ${requestError.message}")
                if (info.canRequestAds()) initSdk(activity)
            },
        )

        // Consent already gathered in a previous session: don't wait for the network round-trip.
        if (info.canRequestAds()) initSdk(activity)
    }

    /** True when the region requires an in-app entry point to change consent (Settings shows it). */
    fun isPrivacyOptionsRequired(context: Context): Boolean =
        UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) Log.w(TAG, "Privacy options: ${formError.errorCode} ${formError.message}")
        }
    }

    private fun initSdk(context: Context) {
        if (!sdkInitialized.compareAndSet(false, true)) return
        runCatching { MobileAds.initialize(context.applicationContext) {} }
            .onFailure { Log.w(TAG, "MobileAds init failed", it) }
        _canRequestAds.value = true
    }

    /**
     * Debug builds simulate an EEA user so the consent form can be exercised locally. UMP only
     * honours this on registered test devices: add the hashed id that logcat prints
     * ("Use new ConsentDebugSettings.Builder().addTestDeviceHashedId("...")") below.
     */
    private fun debugSettings(context: Context): ConsentDebugSettings =
        ConsentDebugSettings.Builder(context)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            // .addTestDeviceHashedId("PASTE_HASH_FROM_LOGCAT")
            .build()
}
