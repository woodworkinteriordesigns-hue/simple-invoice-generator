package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Set to false for Live Google AdMob ads with real account
    var isTestMode: Boolean = false

    // Real Google AdMob ad unit IDs
    private const val REAL_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-5392417974634524/3063255915"
    private const val REAL_BANNER_AD_UNIT_ID = "ca-app-pub-5392417974634524/4419975165"

    // Google's official guaranteed-working test ad units
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    val currentBannerAdUnitId: String
        get() = if (isTestMode) TEST_BANNER_AD_UNIT_ID else REAL_BANNER_AD_UNIT_ID

    val currentInterstitialAdUnitId: String
        get() = if (isTestMode) TEST_INTERSTITIAL_AD_UNIT_ID else REAL_INTERSTITIAL_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false
    private var isInitialized: Boolean = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                MobileAds.initialize(context) { initializationStatus ->
                    Log.d(TAG, "AdMob MobileAds initialized: $initializationStatus")
                    isInitialized = true
                    loadInterstitial(context)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error initializing MobileAds", t)
            }
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isAdLoading) return

        try {
            isAdLoading = true
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                currentInterstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully")
                        interstitialAd = ad
                        isAdLoading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load: ${error.message} (code: ${error.code})")
                        interstitialAd = null
                        isAdLoading = false
                    }
                }
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Error loading interstitial ad", t)
            isAdLoading = false
            interstitialAd = null
        }
    }

    fun showInterstitial(
        activity: Activity?,
        onAdClosed: () -> Unit = {}
    ) {
        val ad = interstitialAd
        if (ad != null && activity != null && !activity.isFinishing && !activity.isDestroyed) {
            try {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad dismissed")
                        interstitialAd = null
                        loadInterstitial(activity.applicationContext)
                        activity.runOnUiThread {
                            onAdClosed()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                        interstitialAd = null
                        loadInterstitial(activity.applicationContext)
                        activity.runOnUiThread {
                            onAdClosed()
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad showed full screen")
                        interstitialAd = null
                    }
                }
                ad.show(activity)
            } catch (t: Throwable) {
                Log.e(TAG, "Error showing interstitial ad", t)
                interstitialAd = null
                onAdClosed()
            }
        } else {
            // No ad ready or not displayable; ensure next one loads in background
            if (activity != null) {
                loadInterstitial(activity.applicationContext)
            }
            onAdClosed()
        }
    }
}
