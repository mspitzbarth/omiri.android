package com.example.omiri.viewmodels

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.R
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.repository.NotificationRepository
import com.example.omiri.ui.models.NotificationUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    
    private val _shoppingListNotifications = MutableStateFlow(true)
    val shoppingListNotifications: StateFlow<Boolean> = _shoppingListNotifications.asStateFlow()

    private val _debugMode = MutableStateFlow(false)
    val debugMode: StateFlow<Boolean> = _debugMode.asStateFlow()

    private val _selectedStoresCount = MutableStateFlow(0)
    val selectedStoresCount: StateFlow<Int> = _selectedStoresCount.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    private val _selectedStoreIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedStoreIds: StateFlow<Set<String>> = _selectedStoreIds.asStateFlow()

    private val _showDummyData = MutableStateFlow(false)
    val showDummyData: StateFlow<Boolean> = _showDummyData.asStateFlow()

    private val _allStores = MutableStateFlow<List<com.example.omiri.data.api.models.StoreListResponse>>(emptyList())
    val allStores: StateFlow<List<com.example.omiri.data.api.models.StoreListResponse>> = _allStores.asStateFlow()

    private val context = application.applicationContext

    companion object {
        private const val CHANNEL_ID = "omiri_debug_channel"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
        loadSelectedStoresCount()
    }
    
    private fun loadSelectedStoresCount() {
        viewModelScope.launch {
            userPreferences.selectedStores.collect { stores ->
                _selectedStoresCount.value = stores.size
                _selectedStoreIds.value = stores
            }
        }
        viewModelScope.launch {
            userPreferences.isOnboardingCompleted.collect { completed ->
                _isOnboardingCompleted.value = completed
            }
        }
        viewModelScope.launch {
            userPreferences.showDummyChatData.collect { show ->
                 _showDummyData.value = show
            }
        }
        // Fetch all stores for Popular Stores section
        viewModelScope.launch {
            try {
                // Assuming "DE" as default for now, or fetch from prefs if available
                val result = com.example.omiri.data.repository.StoreRepository.instance.getStores("DE")
                result.onSuccess { stores ->
                    _allStores.value = stores
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(true)
        }
    }
    
    fun toggleShowDummyData() {
        viewModelScope.launch {
            userPreferences.setShowDummyChatData(!_showDummyData.value)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Omiri Debug"
            val descriptionText = "Debug notifications for Omiri app"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun toggleShoppingListNotifications() {
        _shoppingListNotifications.value = !_shoppingListNotifications.value
        if (_shoppingListNotifications.value) {
            showNotification(
                "Shopping List Notifications",
                "You'll now receive alerts for your list items",
                "🔔"
            )
        }
    }

    fun toggleDebugMode() {
        _debugMode.value = !_debugMode.value
        if (_debugMode.value) {
            showDebugNotification()
        } else {
            showNotification(
                "Debug Mode Disabled",
                "Developer options are now hidden",
                "🛑"
            )
        }
    }

    // Debug Triggers
    fun triggerFlashSaleNotification() {
        // System Notification
        showNotification(
            "Flash Sale Alert!",
            "Hamburger patties at Target now 30% off! Limited time only.",
            "🔥"
        )
        
        // App Notification
        val notification = NotificationUiModel.FlashSale(
            id = UUID.randomUUID().toString(),
            title = "Flash Sale Alert!",
            time = "Just now",
            isRead = false,
            icon = Icons.Outlined.LocalFireDepartment,
            iconColor = Color(0xFFEF4444),
            description = "Hamburger patties at Target now 30% off! Limited time only.",
            timeLeft = "2 hours left",
            savings = "Save $3.60"
        )
        NotificationRepository.instance.addNotification(notification)
    }

    fun triggerPriceDropNotification() {
         // System Notification
        showNotification(
            "Price Drop Alert",
            "Wireless Headphones dropped from $89 to $64 at Best Buy.",
            "📉"
        )
        
        // App Notification
        val notification = NotificationUiModel.PriceDrop(
            id = UUID.randomUUID().toString(),
            title = "Price Drop",
            time = "Just now",
            isRead = false,
            icon = Icons.Outlined.ArrowDownward,
            iconColor = Color(0xFF10B981),
            description = "Wireless Headphones dropped from $89 to $64 at Best Buy",
            currentPrice = "$64",
            originalPrice = "$89",
            discountPercentage = "28% off"
        )
        NotificationRepository.instance.addNotification(notification)
    }

    fun triggerListUpdateNotification() {
         // System Notification
        showNotification(
            "Shopping List Update",
            "3 items from 'BBQ Party List' are now on sale nearby.",
            "🛒"
        )
        
        // App Notification
        val notification = NotificationUiModel.ListUpdate(
            id = UUID.randomUUID().toString(),
            title = "Shopping List Update",
            time = "Just now",
            isRead = false,
            // Using AutoMirrored icon correctly
            icon = Icons.AutoMirrored.Outlined.List,
            iconColor = Color(0xFF3B82F6),
            description = "3 items from \"BBQ Party List\" are now on sale nearby",
            locationTag = "Walmart • 2.3 mi"
        )
        NotificationRepository.instance.addNotification(notification)
    }

    private fun showDebugNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚀 Debug Mode Activated!")
            .setContentText("Developer options are now enabled. Happy debugging!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Debug mode is now active!\n\n✅ Enhanced logging enabled\n✅ Performance metrics visible\n✅ Developer tools unlocked\n\nRemember to disable this in production!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFFE8357.toInt())

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Handle permission denial gracefully
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(title: String, message: String, emoji: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFFFE8357.toInt())

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID + (0..1000).random(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
