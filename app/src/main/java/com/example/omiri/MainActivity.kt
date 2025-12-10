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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // ... existing onCreate ...


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
        // Initialize ProductViewModel for pre-loading data
        val productViewModel: com.example.omiri.viewmodels.ProductViewModel by viewModels()

        setContent {
            val isOnboardingCompleted by settingsViewModel.isOnboardingCompleted.collectAsState()
            
            // Observe Product Data State
            val featuredDeals by productViewModel.featuredDeals.collectAsState()
            val productError by productViewModel.error.collectAsState()
            val loadingProgress by productViewModel.loadingProgress.collectAsState()
            
            // State to control Splash visibility manually for the delay
            // OPTIMIZATION: Show Skeleton immediately if we have ANY indication of progress, 
            // or just always start with UI (Splash will overlay if needed, but we want it GONE fast).
            // Actually, let's dismiss Splash immediately if we are loading (so skeletons show).
            // Current logic: loadingProgress < 1.0f -> Splash Visible.
            // New logic: loadingProgress < 0.1f -> Splash Visible (Only for very first split second).
            // Or better: `isLoading` is true? Show Skeletons. Splash is only for "App Init".
            // Let's set initial state to `false` (Hidden) and let the UI show skeletons?
            // But we need to handle "App Init" (Theme, etc).
            // Let's revert to: Splash stays until `loadingProgress > 0.2f` (Prefs loaded).
            var isSplashVisible by remember { mutableStateOf(loadingProgress < 0.2f) }
            
            // Trigger load once
            LaunchedEffect(Unit) {
                productViewModel.initialLoad()
            }
            
            // Handle Transition Delay when progress hits 100%
            LaunchedEffect(loadingProgress, isOnboardingCompleted) {
                if (loadingProgress >= 1.0f) {
                    isSplashVisible = false
                }
                
                // If not onboarded (new user), we might not reach 1.0 product progress if we skip it?
                // But initialLoad only runs if data is needed. 
                // However, logical safety: If Onboarding is FALSE (i.e. 'onboarding' route), we should hide splash too.
                // But typically onboarding null -> loading prefs.
                // onBoarding false -> new user.
                
                // If isOnboardingCompleted is known AND it is FALSE (New User), hide splash immediately (or after prefs load).
                // Actually, wait for prefs (which should be fast).
                if (isOnboardingCompleted == false) {
                     isSplashVisible = false
                }
            }

            // Fallback: If error occurs, hide splash so user isn't stuck
            LaunchedEffect(productError) {
                if (productError != null) {
                    isSplashVisible = false
                }
            }
            
            OmiriTheme {
                if (!isSplashVisible) {
                    val startDest = if (isOnboardingCompleted == true) com.example.omiri.ui.navigation.Routes.Home else "onboarding"
                    AppNavGraph(
                        startDestination = startDest,
                        settingsViewModel = settingsViewModel
                    )
                } else {
                    // Splash Screen or Loading
                    com.example.omiri.ui.screens.SplashScreen(progress = loadingProgress)
                }
            }
        }
    }
    
    private fun setupBackgroundWorker() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
                
            val workManager = androidx.work.WorkManager.getInstance(this)
            
            // 1. Periodic Work (Every 1 hour)
            val periodicWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.omiri.workers.ShoppingListWorker>(1, java.util.concurrent.TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "ShoppingListWorker_Periodic",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing if running
                periodicWorkRequest
            )
            
            // 2. One Time Work (Immediate check on app start)
            // This ensures "before app starts" behavior effectively if app was killed and restarted, 
            // or verifies logic immediately for the user.
            val oneTimeWorkRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.omiri.workers.ShoppingListWorker>()
                .setConstraints(constraints)
                .build()
                
            workManager.enqueueUniqueWork(
                "ShoppingListWorker_Immediate",
                androidx.work.ExistingWorkPolicy.REPLACE, // Replace old immediate checks
                oneTimeWorkRequest
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
    override fun onStart() {
        super.onStart()
        val userPreferences = com.example.omiri.data.local.UserPreferences(this)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userPreferences.setAppForeground(true)
        }
    }

    override fun onStop() {
        super.onStop()
        val userPreferences = com.example.omiri.data.local.UserPreferences(this)
        // GlobalScope/ProcessLifecycleOwner usage might be safer but for simplicity:
        // Using lifecycleScope might be cancelled before saving? 
        // Using GlobalScope or a dedicated scope is better for "onStop" cleanups.
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userPreferences.setAppForeground(false)
        }
    }
}
