package com.example.omiri.viewmodels

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.omiri.R
import com.example.omiri.data.local.UserPreferences

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    
    private val _shoppingListNotifications = MutableStateFlow(true)
    val shoppingListNotifications: StateFlow<Boolean> = _shoppingListNotifications.asStateFlow()

    private val _monitorAllLists = MutableStateFlow(false)
    val monitorAllLists: StateFlow<Boolean> = _monitorAllLists.asStateFlow()

    private val _debugMode = MutableStateFlow(false)
    val debugMode: StateFlow<Boolean> = _debugMode.asStateFlow()

    private val _selectedStoresCount = MutableStateFlow(0)
    val selectedStoresCount: StateFlow<Int> = _selectedStoresCount.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

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
            }
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

    fun toggleMonitorAllLists() {
        _monitorAllLists.value = !_monitorAllLists.value
        if (_monitorAllLists.value) {
            showNotification(
                "Monitor All Lists",
                "Now tracking all your shopping lists",
                "👀"
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

    private fun showDebugNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚀 Debug Mode Activated!")
            .setContentText("Developer options are now enabled. Happy debugging!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Debug mode is now active!\n\n✅ Enhanced logging enabled\n✅ Performance metrics visible\n✅ Developer tools unlocked\n\nRemember to disable this in production!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFFEA580B.toInt())

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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFFEA580B.toInt())

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID + (0..1000).random(), builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
