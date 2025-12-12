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
    
    // Wrapper for generic caching with timestamp
    data class CachedGenericWrapper<T>(
        val timestamp: Long,
        val data: T
    )

    /**
     * Save cached categories (24h TTL)
     */
    suspend fun saveCachedCategories(categories: List<String>) {
        val wrapper = CachedGenericWrapper(System.currentTimeMillis(), categories)
        val json = gson.toJson(wrapper)
        context.dataStore.edit { preferences ->
            preferences[CACHED_CATEGORIES] = json
        }
    }

    /**
     * Get cached categories (valid for 24h)
     */
    val cachedCategories: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val json = preferences[CACHED_CATEGORIES] ?: return@map emptyList()
        try {
            val type = object : TypeToken<CachedGenericWrapper<List<String>>>() {}.type
            val wrapper: CachedGenericWrapper<List<String>> = gson.fromJson(json, type)
            
            val ttl = 24 * 60 * 60 * 1000L // 24 Hours
            if (System.currentTimeMillis() - wrapper.timestamp < ttl) {
                wrapper.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save cached stores for a country (24h TTL)
     */
    suspend fun saveCachedStores(country: String, stores: List<com.example.omiri.data.api.models.StoreListResponse>) {
        val wrapper = CachedGenericWrapper(System.currentTimeMillis(), stores)
        val json = gson.toJson(wrapper)
        val key = stringPreferencesKey("${CACHED_STORES_PREFIX}$country")
        context.dataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    /**
     * Get cached stores for a country (valid for 24h)
     */
    fun getCachedStores(country: String): Flow<List<com.example.omiri.data.api.models.StoreListResponse>> {
        val key = stringPreferencesKey("${CACHED_STORES_PREFIX}$country")
        return context.dataStore.data.map { preferences ->
            val json = preferences[key] ?: return@map emptyList()
            try {
                val type = object : TypeToken<CachedGenericWrapper<List<com.example.omiri.data.api.models.StoreListResponse>>>() {}.type
                val wrapper: CachedGenericWrapper<List<com.example.omiri.data.api.models.StoreListResponse>> = gson.fromJson(json, type)
                
                val ttl = 24 * 60 * 60 * 1000L // 24 Hours
                if (System.currentTimeMillis() - wrapper.timestamp < ttl) {
                    wrapper.data
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
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
    
    /**
     * Get cached Smart Plan
     */
    val smartPlan: Flow<com.example.omiri.data.api.models.ShoppingListOptimizeResponse?> = context.dataStore.data.map { preferences ->
        val json = preferences[SMART_PLAN] ?: ""
        if (json.isNotEmpty()) {
            try {
                val type = object : TypeToken<com.example.omiri.data.api.models.ShoppingListOptimizeResponse>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Save Smart Plan
     */
    suspend fun saveSmartPlan(plan: com.example.omiri.data.api.models.ShoppingListOptimizeResponse?) {
        val json = if (plan != null) gson.toJson(plan) else ""
        context.dataStore.edit { preferences ->
            preferences[SMART_PLAN] = json
        }
    }
    
    /**
     * Get cached Shopping List Matches Response (for offline/startup calculation)
     */
    val shoppingListMatchesResponse: Flow<com.example.omiri.data.api.models.ShoppingListSearchResponse?> = context.dataStore.data.map { preferences ->
        val json = preferences[SHOPPING_LIST_MATCHES_RESPONSE] ?: ""
        if (json.isNotEmpty()) {
            try {
                val type = object : TypeToken<com.example.omiri.data.api.models.ShoppingListSearchResponse>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Save Shopping List Matches Response
     */
    suspend fun saveShoppingListMatchesResponse(response: com.example.omiri.data.api.models.ShoppingListSearchResponse?) {
        val json = if (response != null) gson.toJson(response) else ""
        context.dataStore.edit { preferences ->
            preferences[SHOPPING_LIST_MATCHES_RESPONSE] = json
        }
    }

    // Wrapper for cache with timestamp is already defined above? No, I see it in lines 159-162. 
    // And `saveCachedProducts` / `getCachedProducts` were also deleted?
    // Let me check Step 512 content.
    // Lines 159-162: `data class CachedGenericWrapper`.
    // Lines 164-228: `saveCachedCategories`, `cachedCategories`, `saveCachedStores`, `getCachedStores`.
    // But `saveCachedProducts` and `getCachedProducts` are MISSING.
    // And `CachedGenericWrapper` for products (lines 295-298 in original) might be different or I can reuse `CachedGenericWrapper`?
    // Original used `CachedProductsWrapper` which hardcoded `List<ProductResponse>`.
    // I should probably restore `CachedProductsWrapper` and its methods too to avoid breaking existing code.
    
    // Wrapper for cache with timestamp (Original)
    data class CachedProductsWrapper(
        val timestamp: Long,
        val products: List<com.example.omiri.data.api.models.ProductResponse>
    )

    /**
     * Save cached products
     */
    suspend fun saveCachedProducts(type: String, products: List<com.example.omiri.data.api.models.ProductResponse>) {
        val key = when(type) {
            "featured" -> CACHED_FEATURED_PRODUCTS
            "all_deals" -> CACHED_ALL_PRODUCTS
            "shopping_list_deals" -> CACHED_SHOPPING_LIST_DEALS
            else -> return
        }
        
        // Wrap with current time and limit to 50
        val wrapper = CachedProductsWrapper(
            timestamp = System.currentTimeMillis(),
            products = products.take(50)
        )
        val json = gson.toJson(wrapper)
        context.dataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    /**
     * Get cached products
     */
    fun getCachedProducts(type: String): Flow<List<com.example.omiri.data.api.models.ProductResponse>> {
        val key = when(type) {
            "featured" -> CACHED_FEATURED_PRODUCTS
            "all_deals" -> CACHED_ALL_PRODUCTS
            "shopping_list_deals" -> CACHED_SHOPPING_LIST_DEALS
            else -> return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return context.dataStore.data.map { preferences ->
            val json = preferences[key] ?: return@map emptyList()
            try {
                // Try to parse as Wrapper (New Format)
                val wrapper = gson.fromJson(json, CachedProductsWrapper::class.java)
                
                // 1. Check TTL (2 Hours)
                val ttl = 2 * 60 * 60 * 1000L
                val now = System.currentTimeMillis()
                if (now - wrapper.timestamp > ttl) {
                    return@map emptyList()
                }
                
                // 2. Filter expired products
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                wrapper.products.filter { product ->
                    val expiry = product.availableUntil
                    if (expiry != null) {
                       try {
                           val date = dateFormat.parse(expiry)
                           date != null && date.time > now
                       } catch (e: Exception) {
                           true 
                       }
                    } else {
                        true
                    }
                }
            } catch (e: Exception) {
                // Fallback for old format (List<ProductResponse>) or general errors
                try {
                     val typeToken = object : TypeToken<List<com.example.omiri.data.api.models.ProductResponse>>() {}.type
                     gson.fromJson(json, typeToken)
                } catch (e2: Exception) {
                    emptyList()
                }
            }
        }
    }

    /**
     * Get favorite deal IDs
     */
    val favoriteDealIds: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_DEAL_IDS] ?: emptySet()
    }

    /**
     * Save favorite deal IDs
     */
    suspend fun saveFavoriteDealIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITE_DEAL_IDS] = ids
        }
    }

    /**
     * Get shopping list deal IDs
     */
    val shoppingListDealIds: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[SHOPPING_LIST_DEAL_IDS] ?: emptySet()
    }

    /**
     * Save shopping list deal IDs
     */
    suspend fun saveShoppingListDealIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SHOPPING_LIST_DEAL_IDS] = ids
        }
    }


    /**
     * Get show dummy chat data status
     */
    val showDummyChatData: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_DUMMY_CHAT_DATA] ?: false
    }

    /**
     * Set show dummy chat data status
     */
    suspend fun setShowDummyChatData(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_DUMMY_CHAT_DATA] = show
        }
    }

    /**
     * Get user gender
     */
    val userGender: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_GENDER]
    }

    /**
     * Save user gender
     */
    suspend fun saveUserGender(gender: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_GENDER] = gender
        }
    }

    /**
     * Get user age range
     */
    val userAgeRange: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_AGE_RANGE]
    }

    /**
     * Save user age range
     */
    suspend fun saveUserAgeRange(ageRange: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_AGE_RANGE] = ageRange
        }
    }

    /**
     * Get user interests
     */
    val userInterests: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[USER_INTERESTS] ?: emptySet()
    }

    /**
     * Save user interests
     */
    suspend fun saveUserInterests(interests: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[USER_INTERESTS] = interests
        }
    }

    companion object {
        private val SELECTED_COUNTRY = stringPreferencesKey("selected_country")
        private val SELECTED_STORES = stringSetPreferencesKey("selected_stores")
        private val USER_GENDER = stringPreferencesKey("user_gender")
        private val USER_AGE_RANGE = stringPreferencesKey("user_age_range")
        private val USER_INTERESTS = stringSetPreferencesKey("user_interests")
        private val SHOPPING_LIST_ITEMS = stringPreferencesKey("shopping_list_items")
        private val SHOPPING_LIST_DEAL_IDS = stringSetPreferencesKey("shopping_list_deal_ids")
        private val MEMBERSHIP_CARDS = stringPreferencesKey("membership_cards")
        private val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        private val SAVED_SHOPPING_LISTS = stringPreferencesKey("saved_shopping_lists")
        private val IS_APP_FOREGROUND = booleanPreferencesKey("is_app_foreground")
        private val STORE_LOCATIONS_PREFIX = "store_locations_"
        
        // Debug Flag
        private val SHOW_DUMMY_CHAT_DATA = booleanPreferencesKey("show_dummy_chat_data")
        
        // Favorites
        private val FAVORITE_DEAL_IDS = stringSetPreferencesKey("favorite_deal_ids")
        
        // Default country
        const val DEFAULT_COUNTRY = "US"
        
        private val CACHED_RETAILERS_STRING = stringPreferencesKey("cached_retailers_string")
        private val SMART_PLAN = stringPreferencesKey("smart_plan")
        private val SHOPPING_LIST_MATCHES_RESPONSE = stringPreferencesKey("shopping_list_matches_response")
        
        // Product Caching Keys
        private val CACHED_FEATURED_PRODUCTS = stringPreferencesKey("cached_featured_products")
        private val CACHED_ALL_PRODUCTS = stringPreferencesKey("cached_all_products")
        private val CACHED_SHOPPING_LIST_DEALS = stringPreferencesKey("cached_shopping_list_deals")
        
        // Heavy Caching Keys
        private val CACHED_CATEGORIES = stringPreferencesKey("cached_categories")
        private val CACHED_STORES_PREFIX = "cached_stores_"
    }
}
