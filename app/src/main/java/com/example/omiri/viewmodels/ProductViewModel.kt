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
    
    private val _shoppingListMatches = MutableStateFlow<Map<String, List<Deal>>>(emptyMap())
    val shoppingListMatches: StateFlow<Map<String, List<Deal>>> = _shoppingListMatches.asStateFlow()
    
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
             
             // Initial loads
             // ...
             loadCategoriesIfNeeded()
             // We don't loadProducts() effectively here until we know country...
             // But we can trigger it.
        }
        

        
        // Observe Shopping List changes to keep Smart Plan / Badges in sync automatically
        viewModelScope.launch {
            userPreferences.selectedStores.distinctUntilChanged().collect { stores ->
                Log.d(TAG, "Store selection changed: ${stores.size} stores selected")
                updateAvailableStores(stores)
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

    // Categories definition moved to top


    fun loadCategoriesIfNeeded() {
        if (_categories.value.isEmpty()) {
            loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
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
    
    /**
     * Core fetching logic
     */
    private suspend fun fetchProducts() {
        Log.d(TAG, "fetchProducts() called at ${System.currentTimeMillis()}")
        val start = System.currentTimeMillis()
        _isLoading.value = true
        _error.value = null
        
        try {
            // Get user's selected country and stores
            val country = userPreferences.selectedCountry.first()
            val selectedStores = userPreferences.selectedStores.first()
            val cachedRetailers = userPreferences.cachedRetailersString.first()
            
            Log.d(TAG, "Prefs loaded in fetchProducts")
            
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
             
            // Parallelize API calls for speed
            val deferredFeatured = viewModelScope.async { loadFeaturedDeals(country, retailers) }
            val deferredAll = viewModelScope.async { loadAllDeals(country, retailers) }
            
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
            
            // Cache successful result
            userPreferences.saveCachedProducts("featured", response.products)
            
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

    fun checkShoppingListMatches(itemsList: List<String>? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Determine items to check: Passed list OR saved list from prefs
                val itemsString = if (itemsList != null) {
                     itemsList.joinToString(",") 
                } else {
                     userPreferences.shoppingListItems.first()
                }
                
                if (itemsString.isBlank()) {
                    _shoppingListMatches.value = emptyMap()
                    _shoppingListDeals.value = emptyList()
                    _matchedDealIds.clear()
                    generateSmartPlan() // Should clear plan
                    _isLoading.value = false
                    return@launch
                }
                
                val items = itemsString // Repository expects comma-separated string
                val requestedItemsList = itemsString.split(",").map { it.trim() }
                
                val country = userPreferences.selectedCountry.first()
                val selectedStores = userPreferences.selectedStores.first()
                val storesForCountry = selectedStores.filter { it.endsWith("_$country", ignoreCase = true) }
                
                val storesResult = storeRepository.getStores(country)
                val allStores = storesResult.getOrNull() ?: emptyList()
                val retailers = if (storesForCountry.isNotEmpty()) {
                    storesForCountry.mapNotNull { id -> allStores.find { it.id == id }?.retailer }.joinToString(",")
                } else null

                val result = repository.searchShoppingList(items, country, retailers)
                

                result.onSuccess { response ->
                    val categories = response.categories ?: emptyMap()
                    
                    val allProducts = categories.values.flatMap { it.products }
                    
                    // Group by searchTerm. If missing, fuzzy match against requested items.
                    val groupedMap = allProducts.groupBy { product -> 
                        product.searchTerm ?: requestedItemsList.find { 
                            product.title.contains(it, ignoreCase = true) 
                        } ?: "Groceries"
                    }
                    
                    val dealMap = groupedMap.mapValues { entry -> 
                        val deals = entry.value.toDeals()
                        // These are definitely on the list, so mark them
                        deals.map { it.copy(isOnShoppingList = true) }
                    }
                        
                    _shoppingListMatches.value = dealMap
                    val flatDeals = dealMap.values.flatten()
                    _shoppingListDeals.value = flatDeals
                    
                    // Update persistent set of matched IDs
                    _matchedDealIds.clear()
                    _matchedDealIds.addAll(flatDeals.map { it.id })
                    
                    // Sync current views immediately
                    _featuredDeals.value = syncDealsState(_featuredDeals.value)
                    _allDeals.value = syncDealsState(_allDeals.value)
                    
                    // Save matches for offline/startup calculation
                    userPreferences.saveShoppingListMatchesResponse(response)
                    
                    generateSmartPlan()
                    
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
    

    

    
    // ... (Keep existing layout but `init` is near top. I am editing lines 83-605? No.
    // I need to be careful. The file content is huge.
    
    // Let's replace `syncDealsWithMatches` first and add the toggle functions. 
    // `_matchedDealIds` is declared at line 83.
    // `syncDealsWithMatches` is at line 600.
    
    // I'll replace `syncDealsWithMatches` block.
    
    // Helper to apply badge state (List & Favorites)
    private fun syncDealsState(deals: List<com.example.omiri.data.models.Deal>): List<com.example.omiri.data.models.Deal> {
        val matched = _matchedDealIds
        val favorites = _favoriteDealIds.value
        
        if (matched.isEmpty() && favorites.isEmpty()) return deals
        
        return deals.map { deal ->
            deal.copy(
                isOnShoppingList = matched.contains(deal.id),
                isFavorite = favorites.contains(deal.id)
            )
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
        viewModelScope.launch {
            val currentItemsStr = userPreferences.shoppingListItems.first()
            val currentItems = currentItemsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            
            // Logic: Toggle based on Product Title or ID?
            // The system uses TEXT matching.
            // If we remove, we remove the text.
            // If we add, we add the text.
            
            // But we also want to mark the specific ID as "matched".
            // Since `_matchedDealIds` is derived from text search, we should add the *Title* to the text list.
            
            val itemToAdd = deal.title.replace(",", "") // Sanitize
            
            // Check if already in list (fuzzy or exact)
            val exists = currentItems.any { it.equals(itemToAdd, ignoreCase = true) } || _matchedDealIds.contains(deal.id)
            
            if (exists) {
                // Remove
                currentItems.removeAll { it.equals(itemToAdd, ignoreCase = true) }
                // Also trigger remove from ID set? `checkShoppingListMatches` will handle sync.
            } else {
                // Add
                currentItems.add(itemToAdd)
                
                // USER REQUEST: Sync Heart
                if (!_favoriteDealIds.value.contains(deal.id)) {
                    toggleFavorite(deal.id)
                }
            }
            
            val newItemsStr = currentItems.joinToString(",")
            userPreferences.saveShoppingListItems(newItemsStr)
            
            // Trigger refresh of matches
            checkShoppingListMatches(currentItems)
        }
    }
    



    
    private fun generateSmartPlan() {
        viewModelScope.launch {
            // ... (existing call to repo) ...
            val result = repository.optimizeShoppingList(maxStores = 3)
            val finalPlan = if (result.isSuccess && result.getOrNull()?.steps?.isNotEmpty() == true) {
                result.getOrNull()
            } else {
                 calculateLocalSmartPlan()
            }
            
            _smartPlan.value = finalPlan
            generateSmartAlerts(finalPlan)
            
            // Persist the plan
            userPreferences.saveSmartPlan(finalPlan)
        }
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
        
        _smartAlerts.value = alerts
    }
    
    private fun calculateLocalSmartPlan(): com.example.omiri.data.api.models.ShoppingListOptimizeResponse? {
        val matches = _shoppingListMatches.value
        if (matches.isEmpty()) return null
        
        // Flatten all deals
        val allDeals = matches.values.flatten()
        if (allDeals.isEmpty()) return null
        
        // Simple strategy: Group by Store, pick top 3 stores with most savings
        // Calculate savings per deal: original - price
        // Map: Store -> Total Savings, List of Items
        
        val storeAnalysis = allDeals.groupBy { it.store }.map { (store, deals) ->
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
        
        // Allow stores with 0 savings if they have matches (e.g. cheapest option)
        // .filter { it.stepSavings > 0 } -> Removed to show Plan even if just matches without discount

        
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


    
    companion object {
        private const val TAG = "ProductViewModel"
    }
}
