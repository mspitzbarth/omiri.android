package com.example.omiri.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.omiri.MainActivity
import com.example.omiri.R
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.repository.ProductRepository
import com.example.omiri.data.repository.StoreRepository
import kotlinx.coroutines.flow.first

class ShoppingListWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository = ProductRepository()
    private val storeRepository = StoreRepository()
    private val userPreferences = UserPreferences(appContext)

    override suspend fun doWork(): Result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        Log.d(TAG, "Starting Shopping List Check...")

        try {
            // ... (keep existing logic structure)
            // 1. Get Shopping List Items
            val items = userPreferences.shoppingListItems.first()
            if (items.isBlank()) {
                Log.d(TAG, "No shopping list items found. Skipping.")
                return@withContext Result.success()
            }

            // 2. Get User Context (Country, Retailers)
            val country = userPreferences.selectedCountry.first()
            val selectedStores = userPreferences.selectedStores.first()
            
            // Map selectedStore IDs to Retailer Names
            val storesForCountry = selectedStores.filter { it.endsWith("_$country", ignoreCase = true) }
            
            val retailers = if (storesForCountry.isNotEmpty()) {
                val storesResult = storeRepository.getStores(country)
                val allStores = storesResult.getOrNull() ?: emptyList()
                storesForCountry.mapNotNull { id -> 
                    allStores.find { it.id == id }?.retailer 
                }.joinToString(",")
            } else null

            // 3. Search API
            Log.d(TAG, "Searching for items: $items in country: $country")
            val result = repository.searchShoppingList(
                items = items,
                country = country,
                retailers = retailers
            )

            // 4. Process Results
            if (result.isSuccess) {
                val response = result.getOrNull()
                val categories = response?.categories
                
                if (!categories.isNullOrEmpty()) {
                    val totalDeals = categories.values.sumOf { it.products.size }
                    if (totalDeals > 0) {
                        Log.d(TAG, "Found $totalDeals deals matching shopping list.")
                        
                        // Check if app is foreground
                        val isForeground = userPreferences.isAppForeground.first()
                        if (!isForeground) {
                             sendNotification(totalDeals, items)
                        } else {
                             Log.d(TAG, "App is in foreground, suppressing notification.")
                        }
                    } else {
                        Log.d(TAG, "No deals found for shopping list.")
                    }
                } else {
                     Log.d(TAG, "No matching categories or deals found.")
                }
            } else {
                Log.e(TAG, "Search failed: ${result.exceptionOrNull()?.message}")
                return@withContext Result.retry()
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in ShoppingListWorker", e)
            return@withContext Result.failure()
        }
    }

    private fun sendNotification(dealCount: Int, items: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create Channel (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shopping List Deals",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Notification Intent
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_FULLSCREEN_AD", true)
            putExtra("NAVIGATE_TO", "shopping_list_matches")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build Notification with guaranteed system icon
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Omiri: Deals Found!")
            .setContentText("Found $dealCount deals for your list: ${items.take(20)}...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "ShoppingListWorker"
        private const val CHANNEL_ID = "shopping_list_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
