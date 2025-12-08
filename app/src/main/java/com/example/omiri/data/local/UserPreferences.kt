package com.example.omiri.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.omiri.data.models.MembershipCard
import com.example.omiri.data.models.ShoppingList
import androidx.datastore.preferences.core.booleanPreferencesKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * User preferences storage using DataStore
 */
class UserPreferences(private val context: Context) {
    private val gson = Gson()
    

    
    /**
     * Get selected country
     */
    val selectedCountry: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_COUNTRY] ?: DEFAULT_COUNTRY
    }
    
    /**
     * Save selected country
     */
    suspend fun saveSelectedCountry(country: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_COUNTRY] = country
        }
    }
    
    /**
     * Get selected store IDs
     */
    val selectedStores: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_STORES] ?: emptySet()
    }
    
    /**
     * Save selected store IDs
     */
    suspend fun saveSelectedStores(storeIds: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_STORES] = storeIds
        }
    }
    
    /**
     * Get selected locations (zipcodes) for a specific store
     */
    fun getStoreLocations(storeId: String): Flow<Set<String>> {
        val key = stringSetPreferencesKey("${STORE_LOCATIONS_PREFIX}$storeId")
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: emptySet()
        }
    }
    
    /**
     * Save selected locations (zipcodes) for a specific store
     */
    suspend fun saveStoreLocations(storeId: String, zipcodes: Set<String>) {
        val key = stringSetPreferencesKey("${STORE_LOCATIONS_PREFIX}$storeId")
        context.dataStore.edit { preferences ->
            preferences[key] = zipcodes
        }
    }
    
    /**
     * Clear all store locations
     */
    suspend fun clearAllStoreLocations() {
        context.dataStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.filter { 
                it.name.startsWith(STORE_LOCATIONS_PREFIX)
            }
            keysToRemove.forEach { key ->
                preferences.remove(key)
            }
        }
    }
    
    /**
     * Clear all preferences
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Get onboarding completion status
     */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Set onboarding completion status
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED] = completed
        }
    }
    
    /**
     * Get saved shopping lists (Full Object Retrieval)
     */
    val savedShoppingLists: Flow<List<ShoppingList>> = context.dataStore.data.map { preferences ->
        val json = preferences[SAVED_SHOPPING_LISTS] ?: ""
        if (json.isNotEmpty()) {
            try {
                // Use list of ShoppingList
                val type = object : TypeToken<List<ShoppingList>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun saveShoppingLists(lists: List<ShoppingList>) {
        val json = gson.toJson(lists)
        context.dataStore.edit { preferences ->
            preferences[SAVED_SHOPPING_LISTS] = json
        }
    }
    
    /**
     * App Foreground Status for Notifications
     */
    val isAppForeground: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_APP_FOREGROUND] ?: false
    }
    
    suspend fun setAppForeground(isForeground: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_APP_FOREGROUND] = isForeground
        }
    }
    
    companion object {
        private val SELECTED_COUNTRY = stringPreferencesKey("selected_country")
        private val SELECTED_STORES = stringSetPreferencesKey("selected_stores")
        private val SHOPPING_LIST_ITEMS = stringPreferencesKey("shopping_list_items")
        private val MEMBERSHIP_CARDS = stringPreferencesKey("membership_cards")
        private val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        private val SAVED_SHOPPING_LISTS = stringPreferencesKey("saved_shopping_lists")
        private val IS_APP_FOREGROUND = booleanPreferencesKey("is_app_foreground")
        private val STORE_LOCATIONS_PREFIX = "store_locations_"
        
        // Default country
        const val DEFAULT_COUNTRY = "US"
        
        private val CACHED_RETAILERS_STRING = stringPreferencesKey("cached_retailers_string")
    }
    
    /**
     * Get cached retailers string
     */
    val cachedRetailersString: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CACHED_RETAILERS_STRING]
    }
    
    /**
     * Save cached retailers string
     */
    suspend fun saveCachedRetailersString(retailers: String) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_RETAILERS_STRING] = retailers
        }
    }
    
    /**
     * Get shopping list items (comma separated)
     */
    val shoppingListItems: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOPPING_LIST_ITEMS] ?: ""
    }
    
    /**
     * Save shopping list items
     */
    suspend fun saveShoppingListItems(items: String) {
        android.util.Log.d("UserPreferences", "Saving shopping list items: $items")
        context.dataStore.edit { preferences ->
            preferences[SHOPPING_LIST_ITEMS] = items
        }
    }
    
    /**
     * Get membership cards
     */
    val membershipCards: Flow<List<MembershipCard>> = context.dataStore.data.map { preferences ->
        val json = preferences[MEMBERSHIP_CARDS] ?: ""
        if (json.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<MembershipCard>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Save membership cards
     */
    suspend fun saveMembershipCards(cards: List<MembershipCard>) {
        val json = gson.toJson(cards)
        context.dataStore.edit { preferences ->
            preferences[MEMBERSHIP_CARDS] = json
        }
    }
}
