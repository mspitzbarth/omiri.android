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
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.omiri.data.api.models.OptimizationStep
import com.example.omiri.data.api.models.ShoppingListOptimizeResponse

/**
 * ViewModel for managing product data from API
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProductRepository()
    private val storeRepository = com.example.omiri.data.repository.StoreRepository()
    private val userPreferences = UserPreferences(application)
    // Inject or get repository singleton
    private val shoppingListRepository = com.example.omiri.data.repository.ShoppingListRepository
    
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

    private val _networkErrorType = MutableStateFlow<com.example.omiri.utils.NetworkErrorType?>(null)
    val networkErrorType: StateFlow<com.example.omiri.utils.NetworkErrorType?> = _networkErrorType.asStateFlow()

    private val _isMockMode = MutableStateFlow(false)
    val isMockMode: StateFlow<Boolean> = _isMockMode.asStateFlow()
    
    private val _shoppingListMatches = MutableStateFlow<Map<String, List<Deal>>>(emptyMap())
    val shoppingListMatches: StateFlow<Map<String, List<Deal>>> = _shoppingListMatches.asStateFlow()

    private val _isPaging = MutableStateFlow(false)
    val isPaging: StateFlow<Boolean> = _isPaging.asStateFlow()
    
    // Pagination state
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    // Categories
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Stores (Moved from below)
    data class StoreFilterOption(val id: String, val name: String, val emoji: String = "🛒")
    private val _availableStores = MutableStateFlow<List<StoreFilterOption>>(emptyList())
    val availableStores: StateFlow<List<StoreFilterOption>> = _availableStores.asStateFlow()

    // Persistent set of IDs valid for the current shopping list
    private val _matchedDealIds =  java.util.Collections.synchronizedSet(mutableSetOf<String>())
    // Persistent set of Favorite IDs
    private val _favoriteDealIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteDealIds: StateFlow<Set<String>> = _favoriteDealIds.asStateFlow()
    
    // Smart Features (Moved from below)
    private val _smartAlerts = MutableStateFlow<List<com.example.omiri.data.api.models.SmartAlert>>(emptyList())
    val smartAlerts: StateFlow<List<com.example.omiri.data.api.models.SmartAlert>> = _smartAlerts.asStateFlow()

    private val _smartPlan = MutableStateFlow<com.example.omiri.data.api.models.ShoppingListOptimizeResponse?>(null)
    val smartPlan: StateFlow<com.example.omiri.data.api.models.ShoppingListOptimizeResponse?> = _smartPlan.asStateFlow()
    
    val monthlySavingsGoal: kotlinx.coroutines.flow.Flow<String> = userPreferences.monthlySavingsGoal
    
    
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
        // Load initial data
        // Load initial data
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
             // Load cached Smart Plan immediately
             val cachedPlan = userPreferences.smartPlan.firstOrNull()
             if (cachedPlan != null) {
                 _smartPlan.value = cachedPlan
                 generateSmartAlerts(cachedPlan)
             }
             
             // Load cached matches (to allow re-calc on startup)
             val cachedMatches = userPreferences.shoppingListMatchesResponse.firstOrNull()
             if (cachedMatches != null) {
                 val categories = cachedMatches.categories ?: emptyMap()
                 val itemsString = userPreferences.shoppingListItems.first()
                 val requestedItemsList = itemsString.split(",").map { it.trim() }
                 
                 val allProducts = categories.values.flatMap { it.products }
                 
                 // Group by searchTerm or fuzzy match (Replicate logic from checkShoppingListMatches)
                 val groupedMap = allProducts.groupBy { product -> 
                        product.searchTerm ?: requestedItemsList.find { 
                            product.title.contains(it, ignoreCase = true) 
                        } ?: "Groceries"
                 }
                 
                 val dealMap = groupedMap.mapValues { entry -> 
                        val deals = entry.value.toDeals()
                        deals.map { it.copy(isOnShoppingList = true) }
                 }
                 
                 _shoppingListMatches.value = dealMap
                 val flatDeals = dealMap.values.flatten()
                 _shoppingListDeals.value = flatDeals
                 
                 _matchedDealIds.clear()
                 _matchedDealIds.addAll(flatDeals.map { it.id })
                 
                 // If cached plan was null, calculate one now using these matches
                 if (_smartPlan.value == null) {
                     val calculatedPlan = calculateLocalSmartPlan()
                     _smartPlan.value = calculatedPlan
                     generateSmartAlerts(calculatedPlan)
                 }
             }
             val cachedFeatured = userPreferences.getCachedProducts("featured").firstOrNull()
             if (!cachedFeatured.isNullOrEmpty()) {
                 _featuredDeals.value = cachedFeatured.toDeals()
             }
             
             val cachedAll = userPreferences.getCachedProducts("all_deals").firstOrNull()
             if (!cachedAll.isNullOrEmpty()) {
                 _allDeals.value = cachedAll.toDeals()
             }

             // NEW: Load cached Shopping List Deals (Generic/Manual Matches)
             // This ensures "Deals for your List" appears instantly
             val cachedShoppingListDeals = userPreferences.getCachedProducts("shopping_list_deals").firstOrNull()
             if (!cachedShoppingListDeals.isNullOrEmpty()) {
                 val deals = cachedShoppingListDeals.toDeals().map { it.copy(isOnShoppingList = true) }
                 _shoppingListDeals.value = deals
                 
                 // Also populate map for "Manually Added" to ensure consistency if needed
                 // But strictly speaking, _shoppingListMatches is the source of truth for the map.
                 // We can try to reconstruct it or just wait for the network refresh.
                 // For now, just setting _shoppingListDeals is enough to show content.
                 
                 _matchedDealIds.addAll(deals.map { it.id })
             }
             
             // Initial loads
             // ...
             loadCategoriesIfNeeded()
             // We don't loadProducts() effectively here until we know country...
             // But we can trigger it.
        }
        

        
        // Observe Shopping List changes to keep Smart Plan / Badges in sync automatically
        viewModelScope.launch {
            var isFirstStoreEmission = true
            userPreferences.selectedStores.distinctUntilChanged().collect { stores ->
                Log.d(TAG, "Store selection changed: ${stores.size} stores selected")
                updateAvailableStores(stores)
                
                if (isFirstStoreEmission) {
                    isFirstStoreEmission = false
                    // Skip reload on first emission to allow initial hydration to proceed naturally
                    return@collect
                }

                // Reload products when stores change (only if we have already loaded data)
                if (_featuredDeals.value.isNotEmpty() || _allDeals.value.isNotEmpty()) {
                    Log.d(TAG, "Reloading products due to store selection change")
                    loadProducts()
                }
            }
        }

        // Observe Favorites
        viewModelScope.launch {
            userPreferences.favoriteDealIds.collect { ids ->
                _favoriteDealIds.value = ids
                _featuredDeals.value = syncDealsState(_featuredDeals.value)
                _allDeals.value = syncDealsState(_allDeals.value)
                _shoppingListDeals.value = syncDealsState(_shoppingListDeals.value)
            }
        }
        
        // Observe Mock Mode Preference
        viewModelScope.launch {
            userPreferences.showDummyChatData.distinctUntilChanged().collect { show ->
                val prevMode = _isMockMode.value
                _isMockMode.value = show
                
                // If mode changed, we must reload everything to switch between Real <-> Mock data
                // Or if enabled and empty, load.
                if (prevMode != show || (show && _featuredDeals.value.isEmpty())) {
                    Log.d(TAG, "Mock Mode changed to $show. Clearing and reloading data.")
                    
                    // Clear existing data so 'loadIfNeeded' validations pass
                    _featuredDeals.value = emptyList()
                    _allDeals.value = emptyList()
                    _shoppingListDeals.value = emptyList()
                    _currentPage.value = 1
                    _hasMore.value = true
                    
                    loadProducts() // Reload Home/Featured
                    // Note: 'All Deals' will be reloaded next time the screen is visited via 'loadAllDealsIfNeeded'
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
            // CRITICAL FIX: If we skip load, we must ensure progress shows as complete
            // so Splash Screen can dismiss.
            _loadingProgress.value = 1.0f
        }
    }

    // Categories definition moved to top


    fun loadCategoriesIfNeeded() {
        if (_categories.value.isEmpty()) {
            loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { // Optimize: IO
            // Check cache first (24h validity)
            val cached = userPreferences.cachedCategories.first()
            if (cached.isNotEmpty()) {
                _categories.value = cached
                return@launch
            }
            
            val result = repository.getCategories()
            result.onSuccess { 
                _categories.value = it
                userPreferences.saveCachedCategories(it)
            }
        }
    }



    private suspend fun getAllStores(country: String): List<com.example.omiri.data.api.models.StoreListResponse> {
        // Check local memory cache first
        if (cachedStores != null && cachedStoresCountry == country) {
            return cachedStores!!
        }
        
        // Check persistent cache (24h validity)
        val persistentCache = userPreferences.getCachedStores(country).first()
        if (persistentCache.isNotEmpty()) {
            cachedStores = persistentCache
            cachedStoresCountry = country
            return persistentCache
        }
        
        // Fetch from API
        val result = storeRepository.getStores(country)
        val stores = result.getOrNull() ?: emptyList()
        if (stores.isNotEmpty()) {
            cachedStores = stores
            cachedStoresCountry = country
            userPreferences.saveCachedStores(country, stores)
        }
        return stores
    }

    private suspend fun updateAvailableStores(selectedStoreIds: Set<String>) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { // Optimize: Default
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
    }

    
    /**
     * Load all products from API (Fire-and-forget)
     */
    fun loadProducts() {
        viewModelScope.launch {
            fetchProducts()
        }
    }

    /**
     * Refresh products and await completion (for Pull-to-Refresh)
     */
    suspend fun refreshProducts() {
        fetchProducts()
    }
    
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    /**
     * Core fetching logic
     */
    private suspend fun fetchProducts() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { // Optimize: IO Context for whole block
            Log.d(TAG, "fetchProducts() called (Sync Mode) at ${System.currentTimeMillis()}")
            val start = System.currentTimeMillis()
            _isLoading.value = true
            _loadingProgress.value = 0.1f // Start
            _isPaging.value = false
            _error.value = null
            
            try {
                // Get user's selected country
                val country = userPreferences.selectedCountry.first()
                val selectedStores = userPreferences.selectedStores.first()
                
                _loadingProgress.value = 0.2f // Preferences loaded
                
                // App Sync
                if (_isMockMode.value) {
                     Log.d(TAG, "Mock Mode: Loading local JSON data")
                     val mockResponse = getMockAppSyncResponse()
                     handleAppSyncResponse(mockResponse, country, selectedStores)
                } else {
                     // Get Stores String for Sync
                     var storesParam: String? = null
                     if (!country.isEmpty() && selectedStores.isNotEmpty()) {
                         try {
                              val allStores = getAllStores(country)
                              // selectedStores contains IDs. We find matching StoreListResponse.
                              val selectedObj = allStores.filter { selectedStores.contains(it.id) }
                              if (selectedObj.isNotEmpty()) {
                                  storesParam = selectedObj.joinToString(",") { "${it.retailer}:${it.id}" }
                              }
                         } catch (e: Exception) {
                             Log.e(TAG, "Failed to build stores param", e)
                         }
                     }
                     
                     // Use new Sync Endpoint
                     val result = repository.getAppSync(country = country, stores = storesParam, activeOnly = true)
                     
                     result.onSuccess { response -> 
                         handleAppSyncResponse(response, country, selectedStores)
                     }.onFailure { error ->
                        val errorMsg = "Sync failed: ${error.message}"
                        Log.e(TAG, errorMsg, error)
                        _error.value = errorMsg
                        _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
                     }
                }
                
            } catch (e: Exception) {
                val errorMsg = "Failed to load products: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
                _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(e)
            } finally {
                _isLoading.value = false
                _loadingProgress.value = 1.0f // Done
            }
        }
    }
    
    private suspend fun handleAppSyncResponse(response: com.example.omiri.data.api.models.AppSyncResponse, country: String?, selectedStores: Set<String>) {
         _loadingProgress.value = 0.8f
         
         // 1. Featured Deals
         val featured = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { 
             response.featuredDeals.toDeals() 
         }
         _featuredDeals.value = featured
         if (!_isMockMode.value) userPreferences.saveCachedProducts("featured", response.featuredDeals)
         
         // 2. Categories
         if (!response.categories.isNullOrEmpty()) {
             _categories.value = response.categories
             if (!_isMockMode.value) userPreferences.saveCachedCategories(response.categories)
         }
         
         // 3. Stores
         if (!response.stores.isNullOrEmpty()) {
             cachedStores = response.stores
             cachedStoresCountry = country
             if (!_isMockMode.value) userPreferences.saveCachedStores(country ?: "DE", response.stores)
             
             // Update available stores filter options
             updateAvailableStores(selectedStores)
             
             // Determine retailers string for cache if needed
             if (country != null) {
                 val storesForCountry = selectedStores.filter { it.endsWith("_$country", ignoreCase = true) }
                 if (storesForCountry.isNotEmpty()) {
                     val retailers = storesForCountry.mapNotNull { id -> 
                         response.stores.find { it.id.toString() == id }?.retailer 
                     }.distinct().joinToString(",")
                     if (!_isMockMode.value) userPreferences.saveCachedRetailersString(retailers)
                 }
             }
         }
         
         Log.d(TAG, "App Sync completed")
         
         // Trigger Smart Plan matches in background
         viewModelScope.launch {
            executeCheckShoppingListMatches() 
         }
    }

    private fun getMockAppSyncResponse(): com.example.omiri.data.api.models.AppSyncResponse {
        return try {
            val assets = getApplication<Application>().assets
            val gson = com.google.gson.Gson()
            
            // Read Products
            val productsJson = assets.open("mock_products.json").bufferedReader().use { it.readText() }
            val productsType = object : com.google.gson.reflect.TypeToken<List<com.example.omiri.data.api.models.ProductResponse>>() {}.type
            val products: List<com.example.omiri.data.api.models.ProductResponse> = gson.fromJson(productsJson, productsType)
            
            // Read Stores
            val storesJson = assets.open("mock_stores.json").bufferedReader().use { it.readText() }
            val storesType = object : com.google.gson.reflect.TypeToken<List<com.example.omiri.data.api.models.StoreListResponse>>() {}.type
            val stores: List<com.example.omiri.data.api.models.StoreListResponse> = gson.fromJson(storesJson, storesType)
            
            val categories = products.flatMap { it.categories ?: emptyList() }.distinct()
            
            com.example.omiri.data.api.models.AppSyncResponse(
                featuredDeals = products.filter { it.featured == true },
                stores = stores,
                categories = categories,
                config = emptyMap()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load mock data", e)
             com.example.omiri.data.api.models.AppSyncResponse(emptyList(), emptyList(), emptyList(), null)
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
            val deals = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { response.products.toDeals() } // Optimize
            _featuredDeals.value = deals
            
            // Cache successful result
            userPreferences.saveCachedProducts("featured", response.products)
            
        }.onFailure { error: Throwable ->
            Log.e(TAG, "Failed to load featured deals", error)
            _error.value = "Failed to load featured deals: ${error.message}"
            _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
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
             val deals = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { response.products.toDeals() } // Optimize
            _shoppingListDeals.value = deals
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load shopping list deals: ${error.message}"
            _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
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
             val deals = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { response.products.toDeals() } // Optimize
            _leavingSoonDeals.value = deals
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load leaving soon deals: ${error.message}"
            _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
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
        _isPaging.value = true // Set paging flag
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { // Optimize: IO
        try {
            val country = userPreferences.selectedCountry.first()
            val selectedStores = userPreferences.selectedStores.first()
            
            val storesForCountry = selectedStores.filter { storeId ->
                 storeId.endsWith("_$country", ignoreCase = true)
            }
            
            val storesResult = storeRepository.getStores(country)
            val allStores = storesResult.getOrNull() ?: emptyList()
            val retailers = if (storesForCountry.isNotEmpty()) {
                storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
            } else null
            
            loadAllDeals(country, retailers)
            
        } catch (e: Exception) {
           Log.e(TAG, "Error loading more deals", e)
           _error.value = "Error loading more: ${e.message}"
        } finally {
            _isLoading.value = false
            _isPaging.value = false
        }

        }
    }

    // Old loadAllDeals handler removed
    
    /**
     * Search products by query
     */
    fun searchProducts(query: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { // Optimize: IO
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = repository.searchProducts(
                    query = query
                )
                
                result.onSuccess { productsMap: Map<String, List<com.example.omiri.data.api.models.ProductResponse>> ->
                    // Flatten the map to a list of products
                     val deals = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        val allProducts = productsMap.values.flatten()
                        allProducts.toDeals()
                    }
                    _allDeals.value = deals
                }.onFailure { error: Throwable ->
                    _error.value = "Search failed: ${error.message}"
                    _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
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

    fun checkShoppingListMatches(itemsList: List<String>? = null) {
        viewModelScope.launch {
            executeCheckShoppingListMatches(itemsList)
        }
    }

    private suspend fun executeCheckShoppingListMatches(itemsList: List<String>? = null) {
        // Optimize: Move Heavy Calculation to Default Dispatcher
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            // Only show loading if we are not already part of a bigger loading flow?
            // But for safety let's just set it. It's StateFlow, duplicates are fine.
            _isLoading.value = true
            _isPaging.value = false
            _error.value = null
            
            try {
                // 1. Get Text Items
                val itemsString = if (itemsList != null) {
                        itemsList.joinToString(",") 
                } else {
                        userPreferences.shoppingListItems.first()
                }
                val requestedItemsList = itemsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                // 2. Get Saved IDs
                val savedIds = userPreferences.shoppingListDealIds.first()
                
                // If both empty, clear everything
                if (requestedItemsList.isEmpty() && savedIds.isEmpty()) {
                    _shoppingListMatches.value = emptyMap()
                    _shoppingListDeals.value = emptyList()
                    _matchedDealIds.clear()
                    executeGenerateSmartPlan() // Suspend version
                    _isLoading.value = false
                    return@withContext
                }
                
                var dealMap: MutableMap<String, List<Deal>> = mutableMapOf()
                
                // 3. Text Search (if items exist)
                // Need IO for Repo call? Yes.
                if (requestedItemsList.isNotEmpty()) {
                    val items = itemsString
                    val country = userPreferences.selectedCountry.first()
                    val selectedStores = userPreferences.selectedStores.first()
                    val storesForCountry = selectedStores.filter { it.endsWith("_$country", ignoreCase = true) }
                    
                    val allStores = getAllStores(country) // Suspend
                    val retailers = if (storesForCountry.isNotEmpty()) {
                        storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
                    } else null

                    // IO Call
                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { 
                        repository.searchShoppingList(items, country, retailers)
                    }
                    
                    result.onSuccess { response ->
                        val categories = response.categories ?: emptyMap()
                        val allProducts = categories.values.flatMap { it.products }
                        
                        val groupedMap = allProducts.groupBy { product -> 
                            product.searchTerm ?: requestedItemsList.find { 
                                product.title.contains(it, ignoreCase = true) 
                            } ?: "Groceries"
                        }
                        
                        dealMap = groupedMap.mapValues { entry -> 
                            val deals = entry.value.toDeals()
                            deals.map { it.copy(isOnShoppingList = true) }
                        }.toMutableMap()
                        
                        userPreferences.saveShoppingListMatchesResponse(response)
                    }
                }
                
                // 4. ID Search (Bulk Fetch Optimization)
                val loadedDeals = _allDeals.value + _featuredDeals.value + _shoppingListDeals.value
                
                // Identify which IDs we already have locally
                val cachedMatches = loadedDeals
                    .filter { savedIds.contains(it.id) }
                    .map { it.copy(isOnShoppingList = true) }
                    .distinctBy { it.id }
                
                val cachedMatchIds = cachedMatches.map { it.id }.toSet()
                val missingIds = savedIds.filter { !cachedMatchIds.contains(it) }
                
                val bulkFetchedDeals = if (missingIds.isNotEmpty()) {
                     kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                         val result = repository.getProductsBulk(missingIds)
                         result.getOrNull()?.toDeals()?.map { it.copy(isOnShoppingList = true) } ?: emptyList()
                     }
                } else {
                    emptyList()
                }
                
                val idDeals = cachedMatches + bulkFetchedDeals
                if (idDeals.isNotEmpty()) {
                    val existing = dealMap["Manually Added"] ?: emptyList()
                    dealMap["Manually Added"] = (existing + idDeals).distinctBy { it.id }
                }
                
                val allMatches = dealMap.values.flatten().distinctBy { it.id }
                
                // Critical: Do NOT update state yet. Calculate Plan FIRST.
                
                // 6. Generate Smart Plan (Suspend, Local)
                executeGenerateSmartPlan(dealMap)
                
                // 2. Update Matches State (NOW it appears in UI)
                _shoppingListMatches.value = dealMap
                _shoppingListDeals.value = allMatches
                
                _matchedDealIds.clear()
                _matchedDealIds.addAll(allMatches.map { it.id } as List<String>)

                // Cache the results to prevent "Start-Up Storm" next time
                if (allMatches.isNotEmpty()) {
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val products = allMatches.map { dealToProductResponse(it) }
                            userPreferences.saveCachedProducts("shopping_list_deals", products)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to cache shopping list deals", e)
                        }
                    }
                }
                
                // 3. Sync View State for badges
                _featuredDeals.value = syncDealsState(_featuredDeals.value)
                _allDeals.value = syncDealsState(_allDeals.value)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking matches", e)
                _error.value = "Error checking matches: ${e.message}"
                _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Helper to apply badge state (List & Favorites)
    private fun syncDealsState(deals: List<com.example.omiri.data.models.Deal>): List<com.example.omiri.data.models.Deal> {
        val matched = _matchedDealIds
        val favorites = _favoriteDealIds.value
        
        if (matched.isEmpty() && favorites.isEmpty()) return deals
        
        // Optimize: Convert matched to HashSet for O(1) lookup if not already (it is syncSet, but iterating list)
        // Ideally should be quick for small N.
        
        return deals.map { deal ->
            // Avoid creating new object if no change?
            val onList = matched.contains(deal.id)
            val isFav = favorites.contains(deal.id)
            
            if (deal.isOnShoppingList != onList || deal.isFavorite != isFav) {
                deal.copy(
                    isOnShoppingList = onList,
                    isFavorite = isFav
                )
            } else {
                deal
            }
        }
    }
    
    fun toggleFavorite(dealId: String) {
        viewModelScope.launch {
            val current = _favoriteDealIds.value.toMutableSet()
            if (current.contains(dealId)) {
                current.remove(dealId)
            } else {
                current.add(dealId)
            }
            userPreferences.saveFavoriteDealIds(current)
        }
    }
    
    fun toggleShoppingList(deal: com.example.omiri.data.models.Deal) {
        val currentDeal = deal // Capture for closure
        viewModelScope.launch {
            // 1. Optimistic Update (Immediate UI Feedback)
            val isAdding = !_matchedDealIds.contains(currentDeal.id)
            
            if (isAdding) {
                // Add to Set
                _matchedDealIds.add(currentDeal.id)
                val newDeal = currentDeal.copy(isOnShoppingList = true)
                
                // Update List
                val currentList = _shoppingListDeals.value.toMutableList()
                if (currentList.none { it.id == newDeal.id }) {
                    currentList.add(newDeal)
                    _shoppingListDeals.value = currentList
                }
                
                // Update Map (Add to "Manually Added" section if not present)
                val currentMap = _shoppingListMatches.value.toMutableMap()
                val manualList = currentMap["Manually Added"]?.toMutableList() ?: mutableListOf()
                if (manualList.none { it.id == newDeal.id }) {
                     manualList.add(newDeal)
                     currentMap["Manually Added"] = manualList.distinctBy { it.id } // Safety
                     _shoppingListMatches.value = currentMap
                }
                
                // Auto-Favorite if needed
                 if (!_favoriteDealIds.value.contains(newDeal.id)) {
                     toggleFavorite(newDeal.id) 
                 }
                
            } else {
                // Remove from Set
                _matchedDealIds.remove(currentDeal.id)
                
                // Update List
                val currentList = _shoppingListDeals.value.filter { it.id != currentDeal.id }
                _shoppingListDeals.value = currentList
                
                // Update Map (Remove from ALL sections)
                val currentMap = _shoppingListMatches.value.toMutableMap()
                val newMap = currentMap.mapValues { (_, deals) -> 
                     deals.filter { it.id != currentDeal.id }
                }.filterValues { it.isNotEmpty() }
                _shoppingListMatches.value = newMap
            }
            
            // 2. Trigger Smart Features Update Immediately (Local calc)
            // This responds to the user "as soon as new products are in the list"
            val localPlan = calculateLocalSmartPlan()
            _smartPlan.value = localPlan
            generateSmartAlerts(localPlan)
            
            // 3. Persistence & Eventual Consistency (Background)
            try {
                val currentItemsStr = userPreferences.shoppingListItems.first()
                val currentItems = currentItemsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                
                val itemToAdd = deal.title.replace(",", "") // Sanitize
                
                // ID Handling for persistence
                val currentIds = userPreferences.shoppingListDealIds.first().toMutableSet()
                val idExists = currentIds.contains(deal.id)
                
                // Check if already in persistent list (fuzzy text or exact ID)
                val exists = currentItems.any { it.equals(itemToAdd, ignoreCase = true) } || idExists
                
                if (exists) {
                    // Remove
                    currentItems.removeAll { it.equals(itemToAdd, ignoreCase = true) }
                    currentIds.remove(deal.id)
                } else {
                    // Add
                    currentItems.add(itemToAdd)
                    currentIds.add(deal.id)
                }
                
                userPreferences.saveShoppingListItems(currentItems.joinToString(","))
                userPreferences.saveShoppingListDealIds(currentIds)
                
                // Trigger full refresh of matches to ensure server-side consistency (e.g. better categorisation)
                // This might overwrite the local plan eventually, but the immediate feedback is already done.
                executeCheckShoppingListMatches(currentItems)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting shopping list change", e)
                // If persistence fails, we might want to revert UI, but for now we log.
            }
        }
    }
    



    
    private fun generateSmartPlan() {
        viewModelScope.launch {
            executeGenerateSmartPlan()
        }
    }

    private suspend fun executeGenerateSmartPlan(matches: Map<String, List<Deal>>? = null) {
        // ... (existing call to repo) ...
        val result = repository.optimizeShoppingList(maxStores = 3)
        val finalPlan = if (result.isSuccess && result.getOrNull()?.steps?.isNotEmpty() == true) {
            result.getOrNull()
        } else {
                calculateLocalSmartPlan(matches)
        }
        
        _smartPlan.value = finalPlan
        generateSmartAlerts(finalPlan)
        
        // Persist the plan
        userPreferences.saveSmartPlan(finalPlan)
    }
    
    private fun generateSmartAlerts(plan: com.example.omiri.data.api.models.ShoppingListOptimizeResponse?) {
        val alerts = mutableListOf<com.example.omiri.data.api.models.SmartAlert>()
        val matches = _shoppingListDeals.value
        
        // 1. Check for significant price drops
        val bigDiscounts = matches.filter { it.discountPercentage >= 30 }
        if (bigDiscounts.isNotEmpty()) {
            val topItem = bigDiscounts.maxByOrNull { it.discountPercentage }!!
            alerts.add(com.example.omiri.data.api.models.SmartAlert(
                title = "${topItem.title} dropped ${topItem.discountPercentage}% at ${topItem.store}",
                type = "PRICE_DROP",
                iconName = "PERCENT"
            ))
        }
        
        // 2. Check for Cheapest Store (lowest total cost from plan)
        if (plan != null && plan.steps.isNotEmpty()) {
             // If valid plan, maybe suggest 1-stop?
             if (plan.steps.size == 1) {
                  alerts.add(com.example.omiri.data.api.models.SmartAlert(
                    title = "Your list can be completed in 1 store: ${plan.steps.first().storeName}",
                    type = "INFO",
                    iconName = "CHECK_CIRCLE"
                ))
             } else {
                 val mainStore = plan.steps.maxByOrNull { it.itemsCount }
                 if (mainStore != null) {
                     alerts.add(com.example.omiri.data.api.models.SmartAlert(
                        title = "${mainStore.storeName} has ${mainStore.itemsCount} of your items cheapest",
                        type = "CHEAPEST",
                        iconName = "HOME"
                    ))
                 }
             }
        }
        
        // 3. Check for expiring deals
        val expiring = matches.filter { it.timeLeftLabel?.contains("hour", ignoreCase = true) == true || it.timeLeftLabel?.contains("today", ignoreCase = true) == true }
        if (expiring.isNotEmpty()) {
             alerts.add(com.example.omiri.data.api.models.SmartAlert(
                title = "${expiring.size} deals expiring today! don't miss out",
                type = "EXPIRING",
                iconName = "CLOCK" // Need to handle icon mapping
            ))
        }
        
        // 4. Fallback: Ensure visibility if we have matches but no specific alerts
        if (alerts.isEmpty() && matches.isNotEmpty()) {
            alerts.add(com.example.omiri.data.api.models.SmartAlert(
                title = "You have ${matches.size} items on your shopping list matched with deals",
                type = "INFO",
                iconName = "CHECK_CIRCLE"
            ))
        }
        
        _smartAlerts.value = alerts
    }
    
    private fun calculateLocalSmartPlan(explicitMatches: Map<String, List<Deal>>? = null): com.example.omiri.data.api.models.ShoppingListOptimizeResponse? {
        val matches = explicitMatches ?: _shoppingListMatches.value
        if (matches.isEmpty()) return null
        
        // Flatten all deals
        val allDeals = matches.values.flatten()
        if (allDeals.isEmpty()) return null
        
        // Simple strategy: Group by Store, pick top 3 stores with most savings
        // Calculate savings per deal: original - price
        // Map: Store -> Total Savings, List of Items
        
        val storeAnalysis = allDeals.groupBy { it.store.takeIf { s -> !s.isNullOrBlank() } ?: "Various Stores" }.map { (store, deals) ->
            val savings = deals.sumOf { deal ->
                val p = deal.price.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                val o = deal.originalPrice?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0
                if (o > p) o - p else 0.0
            }
             val cost = deals.sumOf { it.price.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0 }
             // Use searchTerm (list item name) for counting distinct needs, fall back to title if missing
             val items = deals.map { it.searchTerm ?: it.title }.distinct()
             
             OptimizationStep(
                 storeName = store,
                 storeColor = null,
                 itemsCount = items.size,
                 items = items,
                 stepSavings = savings,
                 totalCost = cost
             )
        }
        
        if (storeAnalysis.isEmpty()) return null
        
        // Pick top 3 stores by: 
        // 1. Coverage (How many items can I get there?)
        // 2. Savings (How much do I save?)
        val topStores = storeAnalysis
            .sortedWith(compareByDescending<OptimizationStep> { it.itemsCount }
            .thenByDescending { it.stepSavings })
            .take(3)
            
        val totalSavings = topStores.sumOf { it.stepSavings }
        val totalCost = topStores.sumOf { it.totalCost }
        
        return com.example.omiri.data.api.models.ShoppingListOptimizeResponse(
            totalSavings = totalSavings,
            originalPrice = totalCost + totalSavings,
            optimizedPrice = totalCost,
            steps = topStores
        )
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

    fun loadAllDealsIfNeeded() {
        if (_allDeals.value.isEmpty() && !_isLoading.value) {
            viewModelScope.launch {
                val country = userPreferences.selectedCountry.first()
                 val selectedStores = userPreferences.selectedStores.first()
                 
                 val storesForCountry = selectedStores.filter { storeId ->
                     storeId.endsWith("_$country", ignoreCase = true)
                 }
                
                 val storesResult = storeRepository.getStores(country)
                 val allStores = storesResult.getOrNull() ?: emptyList()
                 val retailers = if (storesForCountry.isNotEmpty()) {
                     storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
                 } else null

                loadAllDeals(country, retailers)
            }
        }
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
        
        if (_isMockMode.value) {
            // Mock Implementation for Load All Deals
             val mockResp = getMockAppSyncResponse() // Reuse existing loader for PRODUCTS
             // Filter logic (simple simulation)
             val allProds = mockResp.featuredDeals // Actually need ALL products, not just featured. But mock sync returns featured mostly.
             // Let's assume we want to read the full list again to be safe
             val fullProds = try {
                 val assets = getApplication<Application>().assets
                 val gson = com.google.gson.Gson()
                 val productsJson = assets.open("mock_products.json").bufferedReader().use { it.readText() }
                 val productsType = object : com.google.gson.reflect.TypeToken<List<com.example.omiri.data.api.models.ProductResponse>>() {}.type
                 gson.fromJson<List<com.example.omiri.data.api.models.ProductResponse>>(productsJson, productsType)
             } catch (e: Exception) { emptyList() }
             
             // Apply category/price filters loosely
             var filtered = fullProds
             if (activeOnly == true) { /* no-op in mock */ }
             if (_priceRange != null) {
                 filtered = filtered.filter { (it.priceAmount ?: 0.0).toFloat() in _priceRange!! }
             }
             
             _allDeals.value = filtered.toDeals() 
             _hasMore.value = false
             _totalCount.value = filtered.size
             return
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
                
                // Cache first page of results
                // Only if using default filters to avoid caching specific searches as "default"
                if (activeOnly == true && _currentFilterMode == "THIS_WEEK" && _priceRange == null && _sortBy == null) {
                    userPreferences.saveCachedProducts("all_deals", response.products)
                }
            } else {
                // Prevent duplicates which cause LazyColumn crashes
                _allDeals.value = (_allDeals.value + newDeals).distinctBy { it.id }
            }
            _hasMore.value = response.hasMore
            _totalCount.value = response.totalCount
        }.onFailure { error: Throwable ->
            _error.value = "Failed to load all deals: ${error.message}"
            _networkErrorType.value = com.example.omiri.utils.NetworkErrorParser.parseError(error)
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
    /**
     * Get product by ID
     */
    fun getProductById(productId: String, onResult: (Deal?) -> Unit) {
        // 1. Check Memory Cache
        val cachedDeal = _allDeals.value.find { it.id == productId }
            ?: _featuredDeals.value.find { it.id == productId }
            ?: _shoppingListDeals.value.find { it.id == productId }
            ?: _shoppingListMatches.value.values.flatten().find { it.id == productId }
            
        if (cachedDeal != null) {
            Log.d(TAG, "Product $productId found in cache")
            onResult(cachedDeal)
            return
        }

        // 2. Fetch from API
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
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


    
    private fun dealToProductResponse(deal: Deal): com.example.omiri.data.api.models.ProductResponse {
        // Parse price
        val priceVal = deal.price.replace("$", "").replace("€", "").trim().toDoubleOrNull()
        val originalVal = deal.originalPrice?.replace("$", "")?.replace("€", "")?.trim()?.toDoubleOrNull()
        
        return com.example.omiri.data.api.models.ProductResponse(
            id = deal.id,
            pdfId = 0, // Unknown
            title = deal.title,
            description = deal.description,
            brand = deal.brand,
            priceAmount = priceVal,
            priceCurrency = if (deal.price.contains("€")) "EUR" else "USD",
            originalPrice = originalVal,
            discountPercentage = deal.discountPercentage.toDouble(),
            hasDiscount = deal.hasDiscount,
            categories = listOf(deal.category),
            badges = emptyList(), 
            pageNumber = 1,
            confidence = 1.0,
            retailer = deal.store,
            country = deal.country,
            zipcode = deal.zipcode,
            onlineOnly = false,
            featured = false,
            availableFrom = deal.availableFrom,
            availableUntil = deal.availableUntil,
            availabilityText = deal.timeLeftLabel,
            pdfSourceUrl = deal.pdfSourceUrl,
            productImageUrl = deal.imageUrl,
            searchTerm = deal.searchTerm
        )
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
}
