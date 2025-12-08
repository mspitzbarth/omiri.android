package com.example.omiri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import com.example.omiri.ui.navigation.AppNavGraph
import com.example.omiri.ui.theme.OmiriTheme
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Omiri) // Switch back to main theme
        super.onCreate(savedInstanceState)

        // Force dark (black) status bar icons for light backgrounds
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        
        setupBackgroundWorker()
        handleNotificationIntent()

        // Initialize ViewModel for settings/onboarding check
        val settingsViewModel: com.example.omiri.viewmodels.SettingsViewModel by viewModels()

        setContent {
            val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()
            
            OmiriTheme {
                if (isOnboardingCompleted != null) {
                    val startDest = if (isOnboardingCompleted == true) com.example.omiri.ui.navigation.Routes.Home else "onboarding"
                    AppNavGraph(
                        startDestination = startDest,
                        settingsViewModel = settingsViewModel
                    )
                } else {
                    // Splash Screen or Loading
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFEA580B)),
                        contentAlignment = Alignment.Center
                    ) {
                         // Simple Logo or loading
                    }
                }
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
