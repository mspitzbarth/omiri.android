package com.example.omiri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.mappers.toDeal
import com.example.omiri.data.mappers.toDeals
import com.example.omiri.data.models.Deal
import com.example.omiri.data.repository.ProductRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for managing product data from API
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProductRepository()
    private val storeRepository = com.example.omiri.data.repository.StoreRepository()
    private val userPreferences = UserPreferences(application)
    
    // Cache for stores to avoid redundant API calls
    private var cachedStores: List<com.example.omiri.data.api.models.StoreListResponse>? = null
    private var cachedStoresCountry: String? = null
    
    // UI State
    private val _featuredDeals = MutableStateFlow<List<Deal>>(emptyList())
    val featuredDeals: StateFlow<List<Deal>> = _featuredDeals.asStateFlow()
    
    private val _shoppingListDeals = MutableStateFlow<List<Deal>>(emptyList())
    val shoppingListDeals: StateFlow<List<Deal>> = _shoppingListDeals.asStateFlow()
    
    private val _leavingSoonDeals = MutableStateFlow<List<Deal>>(emptyList())
    val leavingSoonDeals: StateFlow<List<Deal>> = _leavingSoonDeals.asStateFlow()
    
    private val _allDeals = MutableStateFlow<List<Deal>>(emptyList())
    val allDeals: StateFlow<List<Deal>> = _allDeals.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _shoppingListMatches = MutableStateFlow<Map<String, List<Deal>>>(emptyMap())
    val shoppingListMatches: StateFlow<Map<String, List<Deal>>> = _shoppingListMatches.asStateFlow()
    
    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    // Savings Calculation
    // We derive this from shoppingListDeals whenever it changes
    val shoppingListSavings: kotlinx.coroutines.flow.Flow<Double> = _shoppingListDeals.map { deals ->
            deals.sumOf { deal ->
                try {
                    val currentPrice = deal.price.replace("$", "").replace("€", "").trim().toDoubleOrNull() ?: 0.0
                    val originalRel = deal.originalPrice?.replace("$", "")?.replace("€", "")?.trim()?.toDoubleOrNull()
                    
                    if (originalRel != null && originalRel > currentPrice) {
                        originalRel - currentPrice
                    } else {
                        0.0
                    }
                } catch (e: Exception) {
                    0.0
                }
            }
        }
    
    init {
        Log.d(TAG, "ProductViewModel initialized")
        
        // Observe store selection changes and reload products automatically
        viewModelScope.launch {
            userPreferences.selectedStores.collect { stores ->
                Log.d(TAG, "Store selection changed: ${stores.size} stores selected")
                updateAvailableStores(stores)
                // Reload products when stores change (only if we have already loaded data)
                if (_featuredDeals.value.isNotEmpty() || _allDeals.value.isNotEmpty()) {
                    Log.d(TAG, "Reloading products due to store selection change")
                    loadProducts()
                }
            }
        }
    }

    /**
     * Trigger initial data load. 
     * Safe to call multiple times - will only load if data is missing.
     */
    fun initialLoad() {
        if (_featuredDeals.value.isEmpty() && !_isLoading.value) {
            Log.d(TAG, "Initial load triggered")
            loadProducts()
            
            // Note: Categories are no longer loaded here. 
            // They are lazy-loaded when the user opens the Filter Modal.
        } else {
            Log.d(TAG, "Initial load skipped - data already present or loading")
        }
    }

    // Categories
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    fun loadCategoriesIfNeeded() {
        if (_categories.value.isEmpty()) {
            loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()
            result.onSuccess { 
                _categories.value = it
            }
        }
    }

    // Stores
    private val _availableStores = MutableStateFlow<List<StoreFilterOption>>(emptyList())
    val availableStores: StateFlow<List<StoreFilterOption>> = _availableStores.asStateFlow()

    data class StoreFilterOption(val id: String, val name: String, val emoji: String = "🛒")

    private suspend fun getAllStores(country: String): List<com.example.omiri.data.api.models.StoreListResponse> {
        if (cachedStores != null && cachedStoresCountry == country) {
            return cachedStores!!
        }
        val result = storeRepository.getStores(country)
        val stores = result.getOrNull() ?: emptyList()
        if (stores.isNotEmpty()) {
            cachedStores = stores
            cachedStoresCountry = country
        }
        return stores
    }

    private suspend fun updateAvailableStores(selectedStoreIds: Set<String>) {
        val country = userPreferences.selectedCountry.first()
        val storesForCountry = selectedStoreIds.filter { it.endsWith("_$country", ignoreCase = true) }
        
        val allStores = getAllStores(country)
        
        val options = storesForCountry.mapNotNull { id ->
            val store = allStores.find { it.id == id }
            if (store != null) {
                StoreFilterOption(id = store.id, name = store.retailer, emoji = "🛒")
            } else null
        }
        
        // Sort: if needed, but for now just list them
        _availableStores.value = options.sortedBy { it.name }
    }

    
    /**
     * Load all products from API
     */
    fun loadProducts() {
        Log.d(TAG, "loadProducts() called at ${System.currentTimeMillis()}")
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "Coroutine started at $start (Diff: ${start - System.currentTimeMillis()})")
            
            try {
                // Get user's selected country and stores
                val country = userPreferences.selectedCountry.first()
                val selectedStores = userPreferences.selectedStores.first()
                val cachedRetailers = userPreferences.cachedRetailersString.first()
                
                Log.d(TAG, "Prefs loaded at ${System.currentTimeMillis()} (Diff from start: ${System.currentTimeMillis() - start})")
                
                var retailers: String? = cachedRetailers
                
                // If cache is missing but we have selected stores, we must fetch store details to map IDs to Names
                if (retailers == null && selectedStores.isNotEmpty()) {
                    Log.d(TAG, "Cache miss for retailers. Fetching stores...")
                    // Filter stores to only include those from the selected country
                    val storesForCountry = selectedStores.filter { storeId ->
                        storeId.endsWith("_$country", ignoreCase = true)
                    }
                    
                    if (storesForCountry.isNotEmpty()) {
                         val allStores = getAllStores(country)
                         retailers = storesForCountry.mapNotNull { id ->
                            allStores.find { it.id == id }?.retailer
                         }.distinct().joinToString(",")
                         
                         // Update cache for next time
                         userPreferences.saveCachedRetailersString(retailers)
                    }
                }
                
                Log.d(TAG, "Retailers resolved: $retailers. Starting API calls at ${System.currentTimeMillis()} (Diff: ${System.currentTimeMillis() - start})")
                
                // Parallelize API calls for speed
                val deferredFeatured = async { loadFeaturedDeals(country, retailers) }
                val deferredAll = async { loadAllDeals(country, retailers) }
                
                deferredFeatured.await()
                deferredAll.await()
                
                Log.d(TAG, "All API calls completed successfully")
            } catch (e: Exception) {
                val errorMsg = "Failed to load products: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load featured deals (products with high discounts)
     */
    private suspend fun loadFeaturedDeals(country: String, retailers: String?) {
        Log.d(TAG, "Loading featured deals...")
        val result = repository.getProducts(
            country = country,
            retailers = retailers,
            hasDiscount = true,
            limit = 25,
            page = 1
        )
        
        result.onSuccess { response: com.example.omiri.data.api.models.ProductsResponse ->
            Log.d(TAG, "Featured deals loaded: ${response.products.size} products (${response.totalCount} total)")
            _featuredDeals.value = response.products.toDeals()
        }.onFailure { error: Throwable ->
            Log.e(TAG, "Failed to load featured deals", error)
            _error.value = "Failed to load featured deals: ${error.message}"
        }
    }
    
    /**
     * Load shopping list deals (general products)
     */
    private suspend fun loadShoppingListDeals(country: String, retailers: String?) {
        val result = repository.getProducts(
            country = country,
            retailers = retailers,
            limit = 25,
            page = 1
        )
        
        result.onSuccess { response: com.example.omiri.data.api.models.ProductsResponse ->
            _shoppingListDeals.value = response.products.toDeals()
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load shopping list deals: ${error.message}"
        }
    }
    
    private suspend fun loadLeavingSoonDeals(country: String, retailers: String?) {
        val result = repository.getProducts(
            country = country,
            retailers = retailers,
            limit = 25,
            page = 1
        )
        
        result.onSuccess { response: com.example.omiri.data.api.models.ProductsResponse ->
            _leavingSoonDeals.value = response.products.toDeals()
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load leaving soon deals: ${error.message}"
        }
    }
    
    /**
     * Load all deals for the all deals screen
     */
    /**
     * Load next page of all deals
     */
    fun loadMoreDeals() {
        if (_isLoading.value || !_hasMore.value) return
        
        Log.d(TAG, "Loading more deals, page ${_currentPage.value + 1}")
        _currentPage.value += 1
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val country = userPreferences.selectedCountry.first()
                val selectedStores = userPreferences.selectedStores.first()
                
                // Re-calculate retailers string (duplicated logic, ideally refactor but fine for now)
                val storesForCountry = selectedStores.filter { storeId ->
                     storeId.endsWith("_$country", ignoreCase = true)
                }
                
                // We need the store list to map IDs to Names again. 
                // To avoid complexity, we can cache retailers or just re-fetch/re-map.
                // Or simplified: fetch directly.
                // For this insertion, I will call loadAllDeals with null logic or handle it.
                // Actually loadAllDeals expects arguments.
                
                // Optimization: Store retailers string in a variable or re-use logic?
                // I'll refactor loadAllDeals to *get* arguments itself or store them?
                // The current structure passes them.
                // I'll reproduce the fetching logic briefly or make a helper.
                
                // Quick fix: Just re-run the retailer mapping logic if needed, or pass null if not strict.
                // But filtering matters.
                // I should probably store `currentRetailers` in the ViewModel state?
                
                // Let's rely on loadProducts's state? No.
                
                // I will duplicate the retailer mapping logic for safety, ensuring consistency.
                val storesResult = storeRepository.getStores(country)
                val allStores = storesResult.getOrNull() ?: emptyList()
                val retailers = if (storesForCountry.isNotEmpty()) {
                    storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
                } else null
                
                loadAllDeals(country, retailers)
                
            } catch (e: Exception) {
               // Handle error
            }
        }
    }

    // Old loadAllDeals handler removed
    
    /**
     * Search products by query
     */
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = repository.searchProducts(
                    query = query
                )
                
                result.onSuccess { productsMap: Map<String, List<com.example.omiri.data.api.models.ProductResponse>> ->
                    // Flatten the map to a list of products
                    val allProducts = productsMap.values.flatten()
                    _allDeals.value = allProducts.toDeals()
                }.onFailure { error: Throwable ->
                    _error.value = "Search failed: ${error.message}"
                }
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Filter State
    private var _currentFilterMode = "THIS_WEEK" // "THIS_WEEK", "NEXT_WEEK"
    private var _priceRange: ClosedFloatingPointRange<Float>? = null
    private var _sortBy: String? = null
    private var _sortOrder: String? = null
    private var _hasDiscount: Boolean = false

    fun setFilterMode(mode: String) {
        if (_currentFilterMode != mode) {
            _currentFilterMode = mode
            _currentPage.value = 1
            _allDeals.value = emptyList() // Clear for skeleton loading
            
            if (mode == "MY_DEALS") {
                checkShoppingListMatches()
            } else {
                loadProducts()
            }
        }
    }

    fun checkShoppingListMatches() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = userPreferences.shoppingListItems.first()
                if (items.isBlank()) {
                    _shoppingListMatches.value = emptyMap()
                    _shoppingListDeals.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }

                val country = userPreferences.selectedCountry.first()
                val selectedStores = userPreferences.selectedStores.first()
                val storesForCountry = selectedStores.filter { it.endsWith("_$country", ignoreCase = true) }
                
                val storesResult = storeRepository.getStores(country)
                val allStores = storesResult.getOrNull() ?: emptyList()
                val retailers = if (storesForCountry.isNotEmpty()) {
                    storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
                } else null

                val result = repository.searchShoppingList(items, country, retailers)
                
                result.onSuccess { map ->
                    val dealMap = map.mapValues { entry -> entry.value.toDeals() }
                    _shoppingListMatches.value = dealMap
                    _shoppingListDeals.value = dealMap.values.flatten()
                }.onFailure {
                    _error.value = "Failed to load matches: ${it.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error checking matches: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setPriceRange(range: ClosedFloatingPointRange<Float>?) {
        if (_priceRange != range) {
            _priceRange = range
            _currentPage.value = 1
             _allDeals.value = emptyList()
            loadProducts()
        }
    }

    fun applyFilters(
        priceRange: ClosedFloatingPointRange<Float>?,
        sortBy: String?,
        sortOrder: String?,
        hasDiscount: Boolean
    ) {
        _priceRange = priceRange
        _sortBy = sortBy
        _sortOrder = sortOrder
        _hasDiscount = hasDiscount
        _currentPage.value = 1
        _allDeals.value = emptyList()
        loadProducts()
    }

    /**
     * Filter products by category (legacy, now handled via loadAllDeals params if we merge)
     * Keeping separate for specific category drill-down if needed, but strict refactor suggests merging into loadAllDeals
     */
    fun filterByCategory(category: String) {
        // ... (kept for compat but ideally use loadAllDeals with category param)
    }

    // Helper to format date YYYY-MM-DD
    private fun formatDate(calendar: Calendar): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(calendar.time)
    }

    private fun getNextWeekRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        // Move to next Monday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Sun=1, Mon=2...
        val daysUntilNextMonday = if (dayOfWeek == Calendar.SUNDAY) 1 else (9 - dayOfWeek)
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextMonday)
        val start = formatDate(calendar)
        
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val end = formatDate(calendar)
        
        return start to end
    }

    /**
     * Load all deals for the all deals screen with filters
     */
    private suspend fun loadAllDeals(country: String, retailers: String?) {
        val page = _currentPage.value
        
        // Date Logic
        var activeOnly: Boolean? = true
        var availableFromMin: String? = null
        var availableFromMax: String? = null
        
        if (_currentFilterMode == "NEXT_WEEK") {
            activeOnly = false // We want future items
            val (start, end) = getNextWeekRange()
            availableFromMin = start
            // availableFromMax could be set if we only want items STARTING next week
            // User request: "next week" -> available in next week?
            // "Show products available from this date or later"
        } else {
             // THIS_WEEK (Default) -> active_only=true (implied now)
        }
        
        val result = repository.getProducts(
            country = country,
            retailer = null,
            retailers = retailers,
            limit = 12,
            page = page,
            minPrice = _priceRange?.start?.toDouble(),
            maxPrice = _priceRange?.endInclusive?.toDouble(),
            // Advanced Filters
            sortBy = _sortBy,
            sortOrder = _sortOrder,
            hasDiscount = if (_hasDiscount) true else null,
            activeOnly = activeOnly,
            availableFromMin = availableFromMin
        )
        
        result.onSuccess { response: com.example.omiri.data.api.models.ProductsResponse ->
            val newDeals = response.products.toDeals()
            if (page == 1) {
                _allDeals.value = newDeals
            } else {
                // Prevent duplicates which cause LazyColumn crashes
                _allDeals.value = (_allDeals.value + newDeals).distinctBy { it.id }
            }
            _hasMore.value = response.hasMore
            _totalCount.value = response.totalCount
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load all deals: ${error.message}"
            if (page > 1) _currentPage.value -= 1
        }
    }
    
    /**
     * Filter products by retailer
     */
    fun filterByRetailer(retailer: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val country = userPreferences.selectedCountry.first()
                val result = repository.getProducts(
                    country = country,
                    retailer = retailer,
                    limit = 25,
                    page = 1
                )
                
                result.onSuccess { response: com.example.omiri.data.api.models.ProductsResponse ->
                    _allDeals.value = response.products.toDeals()
                }.onFailure { error: Throwable ->
                    _error.value = "Filter failed: ${error.message}"
                }
            } catch (e: Exception) {
                _error.value = "Filter failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get product by ID
     */
    fun getProductById(productId: String, onResult: (Deal?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getProductById(productId)
                
                result.onSuccess { product: com.example.omiri.data.api.models.ProductResponse ->
                    onResult(product.toDeal())
                }.onFailure { _: Throwable ->
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }


    
    companion object {
        private const val TAG = "ProductViewModel"
    }
}
