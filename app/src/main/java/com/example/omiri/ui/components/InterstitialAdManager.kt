package com.example.omiri.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manager for Google AdMob Interstitial (fullscreen) ads
 */
class InterstitialAdManager(private val context: Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    companion object {
        private const val TAG = "InterstitialAdManager"
        // TODO: Replace with your actual AdMob Interstitial Ad Unit ID
        // For testing, use: "ca-app-pub-3940256099942544/1033173712"
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }
    
    /**
     * Load an interstitial ad
     */
    fun loadAd() {
        if (isLoading || interstitialAd != null) {
            Log.d(TAG, "Ad already loaded or loading")
            return
        }
        
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isLoading = false
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Failed to load interstitial ad: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }
    
    /**
     * Show the interstitial ad if loaded
     * @param onAdDismissed Callback when ad is dismissed
     */
    fun showAd(onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null && context is Activity) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed")
                    interstitialAd = null
                    onAdDismissed()
                    // Load next ad
                    loadAd()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Ad failed to show: ${error.message}")
                    interstitialAd = null
                    onAdDismissed()
                    // Load next ad
                    loadAd()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content")
                }
            }
            
            ad.show(context)
        } else {
            Log.w(TAG, "Interstitial ad not ready or context not Activity")
            onAdDismissed()
            // Try to load ad for next time
            loadAd()
        }
    }
    
    /**
     * Check if ad is loaded and ready to show
     */
    fun isAdReady(): Boolean = interstitialAd != null
}
