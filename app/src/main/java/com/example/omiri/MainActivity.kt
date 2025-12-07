package com.example.omiri

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import com.example.omiri.ui.navigation.AppNavGraph
import com.example.omiri.ui.theme.OmiriTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Omiri) // Switch back to main theme
        super.onCreate(savedInstanceState)

        // Force dark (black) status bar icons for light backgrounds
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        
        setupBackgroundWorker()
        handleNotificationIntent()

        setContent {
            OmiriTheme {
                AppNavGraph()
            }
        }
    }
    
    private fun setupBackgroundWorker() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
                
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.omiri.workers.ShoppingListWorker>(1, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
                
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ShoppingListWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            // WorkManager might not be initialized or dependency missing
            e.printStackTrace()
        }
    }
    
    private fun handleNotificationIntent() {
        if (intent.getBooleanExtra("SHOW_FULLSCREEN_AD", false)) {
            // Show AdMob Interstitial
            android.widget.Toast.makeText(this, "Loading Ad...", android.widget.Toast.LENGTH_SHORT).show()
            
            try {
                // Initialize MobileAds (idempotent)
                com.google.android.gms.ads.MobileAds.initialize(this) {}
                
                // Load Interstitial (Test ID)
                val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                
                com.google.android.gms.ads.interstitial.InterstitialAd.load(
                    this,
                    "ca-app-pub-3940256099942544/1033173712", // Google Test Ad Unit ID
                    adRequest,
                    object : com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd) {
                            interstitialAd.show(this@MainActivity)
                        }

                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            // Ad failed, proceed or just log
                            android.util.Log.e("MainActivity", "Ad failed to load: ${loadAdError.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
