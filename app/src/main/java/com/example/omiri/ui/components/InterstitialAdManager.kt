package com.example.omiri.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
class InterstitialAdManager(private val context: Context) {
    
    // AdMob removed for now
    
    fun loadAd() {
        // No-op
    }
    
    fun showAd(onAdDismissed: () -> Unit = {}) {
        // No-op
        onAdDismissed()
    }
    
    fun isAdReady(): Boolean = false
}
