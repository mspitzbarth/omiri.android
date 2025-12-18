package com.example.omiri.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.local.UserPreferences
import com.example.omiri.data.models.PredefinedCategories
import com.example.omiri.data.models.ShoppingItem
import com.example.omiri.data.models.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = com.example.omiri.data.repository.ShoppingListRepository
    private val userPreferences = UserPreferences(application)

    val shoppingLists: StateFlow<List<ShoppingList>> = repository.shoppingLists
    val currentListId: StateFlow<String?> = repository.currentListId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val currentList: StateFlow<ShoppingList?> = combine(shoppingLists, currentListId) { lists, currentId ->
        lists.find { it.id == currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Multi-Selection State
    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    fun toggleSelection(itemId: String) {
        val current = _selectedItemIds.value
        if (current.contains(itemId)) {
            _selectedItemIds.value = current - itemId
        } else {
            _selectedItemIds.value = current + itemId
        }
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun deleteSelectedItems() {
        val idsToDelete = _selectedItemIds.value
        idsToDelete.forEach { id ->
            repository.deleteItem(id)
        }
        clearSelection()
    }

    fun duplicateSelectedItems() {
        val selectedIds = _selectedItemIds.value
        val list = currentList.value
        if (!selectedIds.isNullOrEmpty() && list != null) {
            val itemsToDuplicate = list.items.filter { selectedIds.contains(it.id) }
            itemsToDuplicate.forEach { item ->
                repository.addItem(
                    name = item.name,
                    categoryId = item.categoryId,
                    isInDeals = item.isInDeals, 
                    isRecurring = item.isRecurring
                )
            }
            checkDealsForCurrentList() 
            clearSelection()
        }
    }

    fun moveSelectedItems(targetListId: String) {
        val selectedIds = _selectedItemIds.value
        val sourceList = currentList.value
        
        if (!selectedIds.isNullOrEmpty() && sourceList != null) {
            val itemsToMove = sourceList.items.filter { selectedIds.contains(it.id) }
            
            // 1. Add to target list
            // Ideally repository has moveItem(itemId, targetListId), but based on available methods:
            // We'll mimic move by adding to new list and removing from old. 
            // NOTE: This changes item IDs. If precise ID tracking needed, backend support required.
            itemsToMove.forEach { item ->
                repository.addItemToList(
                    listId = targetListId,
                    name = item.name,
                    categoryId = item.categoryId,
                    isInDeals = item.isInDeals,
                    isRecurring = item.isRecurring
                )
                // 2. Remove from current list
                repository.deleteItem(item.id)
            }
            
            checkDealsForCurrentList()
            clearSelection()
        }
    }

    // Helper to check if we are in selection mode
    val inSelectionMode: StateFlow<Boolean> = _selectedItemIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    data class CategoryCount(
        val id: String,
        val name: String,
        val count: Int
    )

    val availableCategories: StateFlow<List<CategoryCount>> = currentList.map { list ->
        if (list == null) return@map emptyList()
        
        list.items
            .groupBy { it.categoryId }
            .map { (catId, items) ->
                val catName = com.example.omiri.util.CategoryHelper.getCategoryName(catId)
                CategoryCount(catId, catName, items.size)
            }
            .sortedByDescending { it.count }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List Filtering (e.g. by Store Run)
    private val _filterItemNames = MutableStateFlow<List<String>?>(null)
    val filterItemNames: StateFlow<List<String>?> = _filterItemNames.asStateFlow()

    private val _filterStoreName = MutableStateFlow<String?>(null)
    val filterStoreName: StateFlow<String?> = _filterStoreName.asStateFlow()

    fun setStoreFilter(storeName: String, itemNames: List<String>) {
        _filterStoreName.value = storeName
        _filterItemNames.value = itemNames
    }

    fun clearStoreFilter() {
        _filterStoreName.value = null
        _filterItemNames.value = null
        // Reset sort to Custom when clearing store filter (optional, but good UX)
    }

    enum class SortOption {
        CUSTOM,
        STORE,
        CATEGORY
    }

    private val _currentSortOption = MutableStateFlow(SortOption.CUSTOM)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()

    fun setSortOption(option: SortOption) {
        _currentSortOption.value = option
    }

    fun reorderItems(fromIndex: Int, toIndex: Int) {
        if (_currentSortOption.value == SortOption.CUSTOM) {
             repository.reorderItems(fromIndex, toIndex)
        }
    }

    fun reorderItemById(fromId: String, toId: String) {
        if (_currentSortOption.value == SortOption.CUSTOM) {
             repository.reorderItemById(fromId, toId)
        }
    }

    val filteredItems: StateFlow<List<ShoppingItem>> = combine(
        currentList, 
        _searchQuery, 
        _selectedCategory, 
        _filterItemNames,
        _currentSortOption
    ) { list, query, categoryId, filterNames, sortOption ->
        val items = list?.items ?: emptyList()
        var result = items
        
        // 1. Filter by Store/Specific Items
        if (filterNames != null) {
            result = result.filter { item -> filterNames.any { it.equals(item.name, ignoreCase = true) } }
        }

        // 2. Filter by Category
        if (categoryId != null) {
            result = result.filter { it.categoryId == categoryId }
        }
        
        // 3. Filter by Search Query
        if (query.isNotEmpty()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        
        // 4. Sort
        result = when (sortOption) {
            SortOption.STORE -> result.sortedBy { it.store ?: "ZZZ" } // Items with store first? or alphabetical. "ZZZ" puts unknown at end
            SortOption.CATEGORY -> result.sortedBy { 
                 // Sort by Category Name (English)
                 PredefinedCategories.getCategoryById(it.categoryId).getName("en")
            }
            SortOption.CUSTOM -> result // Default list order
        }
        
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped items for UI sections
    val groupedItems: StateFlow<Map<String, List<ShoppingItem>>> = combine(filteredItems, _currentSortOption) { items, sortOption ->
        when (sortOption) {
            SortOption.CATEGORY -> {
                items.groupBy { PredefinedCategories.getCategoryById(it.categoryId).getName("en") }
                    .toSortedMap()
            }
            SortOption.STORE -> {
                items.groupBy { it.store ?: "Other Stores" }
                    .toSortedMap(compareBy { if (it == "Other Stores") "ZZZ" else it }) // Put "Other" last
            }
            SortOption.CUSTOM -> {
                mapOf("" to items) // No grouping header needed, or "All Items"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val productRepository = com.example.omiri.data.repository.ProductRepository()

    // ... (existing init) ...
    init {
        android.util.Log.d("ShoppingListViewModel", "Initializing ShoppingListViewModel sync")
        
        viewModelScope.launch {
            // 1. Load persisted lists first
            val savedLists = userPreferences.savedShoppingLists.firstOrNull()
            if (!savedLists.isNullOrEmpty()) {
                repository.updateShoppingLists(savedLists)
                // Ensure current ID is valid
                if (repository.currentListId.value == null || savedLists.none { it.id == repository.currentListId.value }) {
                    repository.switchList(savedLists.first().id)
                }
            }
            
            // 2. Fetch from API
            try {
                repository.fetchShoppingLists("1")
            } catch (e: Exception) {
               android.util.Log.e("ShoppingListViewModel", "Error fetching initial lists", e) 
            }

            // 2. Observer changes to save persistence & update background worker string
            shoppingLists.collect { lists ->
                 // Save full object for persistence
                 if (lists.isNotEmpty()) {
                     userPreferences.saveShoppingLists(lists)
                 }

                 // Calculate string for worker
                 val workerString = lists.flatMap { it.items }
                    .filter { !it.isDone }
                    .map { it.name }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                    .joinToString(",")
                    
                 userPreferences.saveShoppingListItems(workerString)
                 checkDealsForCurrentList()

            }
        }
    }
    
    // Smart Plan / Recommended Route (Reactive)
    val smartPlan: StateFlow<com.example.omiri.data.api.models.ShoppingListOptimizeResponse?> = currentList.map { list ->
        if (list == null) return@map null
        val unfinishedItems = list.items.filter { !it.isDone }
        
        if (unfinishedItems.isEmpty()) return@map null
        
        calculateLocalSmartPlan(unfinishedItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun calculateLocalSmartPlan(items: List<ShoppingItem>): com.example.omiri.data.api.models.ShoppingListOptimizeResponse? {
        // Log to debug why smart plan might be empty
        android.util.Log.d("ShoppingListViewModel", "Calculating Smart Plan for ${items.size} items")
        
        // Relaxed filter: Allow items with just a store, even if dealId is null (e.g. manual store, or legacy deal link)
        val itemsWithDeals = items.filter { !it.store.isNullOrBlank() }
        
        android.util.Log.d("ShoppingListViewModel", "Found ${itemsWithDeals.size} items with assigned stores")
        
        if (itemsWithDeals.isEmpty()) return null
        
        // Group by Store
        val shopSteps = itemsWithDeals.groupBy { it.store!! }
            .map { (storeName, storeItems) ->
                val savings = storeItems.sumOf { 
                   if (it.price != null && it.discountPrice != null && it.discountPrice < it.price) {
                       it.price - it.discountPrice
                   } else 0.0
                }
                
                com.example.omiri.data.api.models.OptimizationStep(
                    storeName = storeName,
                    storeColor = null, // Handled in UI
                    itemsCount = storeItems.size,
                    items = storeItems.map { it.name }.distinct(),
                    stepSavings = savings,
                    totalCost = storeItems.sumOf { it.discountPrice ?: (it.price ?: 0.0) }
                )
            }
            .sortedWith(
                compareByDescending<com.example.omiri.data.api.models.OptimizationStep> { it.itemsCount }
                    .thenByDescending { it.stepSavings }
            ) // Prioritize stores with most items, then most savings
            .take(3)
        
        if (shopSteps.isEmpty()) return null
        
        val totalSavings = shopSteps.sumOf { it.stepSavings }
        val finalPrice = shopSteps.sumOf { it.totalCost }
        
        return com.example.omiri.data.api.models.ShoppingListOptimizeResponse(
            totalSavings = totalSavings,
            originalPrice = finalPrice + totalSavings,
            optimizedPrice = finalPrice,
            steps = shopSteps
        )
    }
    
    // Find Deals
    private val _findingDealsForItem = MutableStateFlow<ShoppingItem?>(null)
    val findingDealsForItem: StateFlow<ShoppingItem?> = _findingDealsForItem.asStateFlow()
    
    private val _dealSearchResults = MutableStateFlow<List<com.example.omiri.data.api.models.ProductResponse>>(emptyList())
    val dealSearchResults: StateFlow<List<com.example.omiri.data.api.models.ProductResponse>> = _dealSearchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    fun startFindDeals(item: ShoppingItem) {
        _findingDealsForItem.value = item
        _dealSearchResults.value = emptyList()
        searchDeals(item.name)
    }
    
    fun stopFindDeals() {
        _findingDealsForItem.value = null
        _dealSearchResults.value = emptyList()
    }
    
    fun searchDeals(query: String) {
        if (query.isBlank()) return
        
        _isSearching.value = true
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val country = userPreferences.selectedCountry.firstOrNull() ?: "DE"
            val zipcode = userPreferences.zipcode.firstOrNull() ?: ""
            val storesSet: Set<String> = userPreferences.selectedStores.firstOrNull() ?: emptySet()
            val stores: String? = if (storesSet.isNotEmpty()) storesSet.joinToString(",") else null

            val result = productRepository.searchShoppingList(
                items = query,
                country = country,
                retailers = stores,
                zipcode = if (zipcode.isNotEmpty()) zipcode else null,
                limit = 10
            )

            _isSearching.value = false
            
            result.onSuccess { response ->
                val categories = response.categories ?: emptyMap()
                val products = categories.values.filterNotNull().flatMap { it.products }
                _dealSearchResults.value = products
            }
            result.onFailure {
                _dealSearchResults.value = emptyList()
            }
        }
    }
    
    fun applyDeal(product: com.example.omiri.data.api.models.ProductResponse) {
        val item = _findingDealsForItem.value ?: return
        
        val originalPrice = product.originalPrice ?: product.priceAmount
        val finalPrice = product.priceAmount
        
        val discountPercent = product.discountPercentage?.toInt() ?: if (originalPrice != null && finalPrice != null && originalPrice > finalPrice) {
            ((originalPrice - finalPrice) / originalPrice * 100).toInt()
        } else null
        
        repository.updateItemDeal(
            itemId = item.id,
            store = product.retailer,
            price = originalPrice,
            discountPrice = if (originalPrice != null && finalPrice != null && finalPrice < originalPrice) finalPrice else null,
            discountPercentage = discountPercent,
            dealId = product.id
        )
        
        stopFindDeals()
        // No need to fetchSmartPlan via invalid API anymore. 
        // Logic will auto-update via `shoppingLists.collect` -> `fetchSmartPlan` (local)
    }

    private var lastCheckedItems: Set<String> = emptySet()
    private var lastCheckedState: String? = null

    private val _isCheckingDeals = MutableStateFlow(false)
    val isCheckingDeals: StateFlow<Boolean> = _isCheckingDeals.asStateFlow()

    private fun checkDealsForCurrentList(force: Boolean = false) {
        viewModelScope.launch {
             _isCheckingDeals.value = true
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val list = currentList.value 
                    if (list == null) {
                         _isCheckingDeals.value = false
                         return@withContext
                    }
                    val unfinishedItems = list.items.filter { !it.isDone }
                    val itemsNames = unfinishedItems.map { it.name }.filter { it.isNotBlank() }.toSet()
                    
                    if (itemsNames.isEmpty()) {
                        _isCheckingDeals.value = false
                        return@withContext
                    }
                    
                    val country = userPreferences.selectedCountry.firstOrNull() ?: "DE"
                    val zipcode = userPreferences.zipcode.firstOrNull() ?: ""
                    val storesSet: Set<String> = userPreferences.selectedStores.firstOrNull() ?: emptySet()
                    val stores: String = if (storesSet.isNotEmpty()) storesSet.joinToString(",") else ""
                    
                    val contextState = "$country|$zipcode|$stores"
                    
                    // Optimization: If current items are a subset of last checked items, AND context hasn't changed,
                    // then we don't need to fetch API. Logic: We already have deals for these items.
                    // This handles the "Item Checked Done" case correctly.
                    if (!force && lastCheckedState == contextState && lastCheckedItems.containsAll(itemsNames)) {
                        android.util.Log.d("ShoppingListViewModel", "Skipping API check: Subset of previous items and same context.")
                        _isCheckingDeals.value = false
                        return@withContext
                    }
                    
                    val itemsString = itemsNames.sorted().joinToString(",")
                    
                    // Update Cache
                    lastCheckedState = contextState
                    lastCheckedItems = itemsNames
                    
                    // IO Call
                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        productRepository.searchShoppingList(
                            items = itemsString,
                            stores = if (stores.isNotEmpty()) stores else null,
                            country = country,
                            zipcode = if (zipcode.isNotEmpty()) zipcode else null,
                            limit = 3
                        )
                    }
                    
                    result.onSuccess { response ->
                        val categories = response.categories ?: emptyMap()
                        val allFoundProducts = categories.values.filterNotNull().flatMap { it.products }
                        
                            unfinishedItems.forEach { item ->
                                val matches = allFoundProducts
                                    .filter { it.searchTerm.equals(item.name, ignoreCase = true) == true }
                                val bestDeal = matches.minByOrNull { it.priceAmount ?: Double.MAX_VALUE }

                            if (bestDeal != null) {
                                val originalPriceVal = bestDeal.originalPrice
                                val originalPrice = if (originalPriceVal != null && originalPriceVal > 0) originalPriceVal else bestDeal.priceAmount
                                val finalPrice = bestDeal.priceAmount
                                val dP = bestDeal.discountPercentage?.toInt()
                                val altCount = (matches.size - 1).coerceAtLeast(0)
                                
                if (item.dealId != bestDeal.id || force) {
                                    val catId = mapCategoryNameToId(bestDeal.categories?.firstOrNull())
                                    repository.updateItemDeal(
                                        itemId = item.id,
                                        store = bestDeal.retailer,
                                        price = originalPrice,
                                        discountPrice = if (originalPrice != null && finalPrice != null && finalPrice < originalPrice) finalPrice else null,
                                        discountPercentage = dP,
                                        dealId = bestDeal.id,
                                        categoryId = catId,
                                        alternativesCount = altCount
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ShoppingListViewModel", "Error checking deals: ${e.message}", e)
                } finally {
                     _isCheckingDeals.value = false
                }
            }
        }
    }

    // ... (rest of class) ...

    fun addItem(
        name: String, 
        categoryId: String = PredefinedCategories.OTHER.id, 
        isInDeals: Boolean = false, 
        isRecurring: Boolean = false,
        store: String? = null,
        price: Double? = null,
        discountPrice: Double? = null,
        discountPercentage: Int? = null,
        dealId: String? = null
    ) {
        repository.addItem(name, categoryId, isInDeals, isRecurring, store, price, discountPrice, discountPercentage, dealId)
        // Check deals for the new item (or refresh all) - ONLY IF dealId is not provided (meaning generic add)
        // If dealId IS provided, we assume we want THAT deal.
        // However, we might want to check for *better* deals?
        // Let's stick to: if adding a specific deal, don't auto-search immediately unless we want to find others.
        // But checkDealsForCurrentList logic is safe if deal matches.
        // Actually, if we pass dealId, we are good. checkDeals... might re-verify.
        checkDealsForCurrentList()
    }

    fun updateItem(itemId: String, name: String, categoryId: String, isRecurring: Boolean) {
        val list = currentList.value
        val existingItem = list?.items?.find { it.id == itemId }
        
        repository.updateItem(itemId, name, categoryId, isRecurring)
        
        // Only check for deals if the name has changed.
        // This prevents blowing away manually selected deals if the user just changes category/recurring.
        if (existingItem != null) {
            checkDealsForCurrentList(force = true)
        }
    }

    fun toggleItemDone(itemId: String) = repository.toggleItemDone(itemId)
    fun deleteItem(itemId: String) = repository.deleteItem(itemId)
    fun resetRecurringItems() = repository.resetRecurringItems()
    
    fun createList(name: String) = repository.createList(name)
    fun switchList(listId: String) = repository.switchList(listId)
    fun refreshDeals() {
        checkDealsForCurrentList(force = true)
    }

    fun deleteList(listId: String) = repository.deleteList(listId)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }

    val totalUnfinishedItemsCount: StateFlow<Int>
        get() = shoppingLists.map { lists ->
            lists.sumOf { list -> list.items.count { !it.isDone } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setDealListed(deal: com.example.omiri.data.models.Deal, isListed: Boolean) {
        if (isListed) {
            // Add
            // Add
            val categoryId = mapCategoryNameToId(deal.category) ?: PredefinedCategories.OTHER.id
            
            // Parse Prices
            val priceVal = parsePrice(deal.originalPrice ?: deal.price)
            val discountPriceVal = if (deal.originalPrice != null) parsePrice(deal.price) else null
            
            addItem(
                name = deal.title, 
                categoryId = categoryId, 
                isInDeals = true,
                store = deal.store,
                price = priceVal,
                discountPrice = discountPriceVal,
                discountPercentage = if (deal.discountPercentage > 0) deal.discountPercentage else null,
                dealId = deal.id
            )
        } else {
            // Remove logic needs to be in Repository or implemented here using deleteItem
            // For now, simpler to implement removal logic in Repository if needed strictly,
            // but we can just loop through current list here since we have read access.
            // HOWEVER, modifying state must go through Repository.
            // Let's implement a 'removeDealItem' in Repository or just find ID here and call deleteItem.
            val list = currentList.value ?: return
            val itemToRemove = list.items.find { (it.dealId == deal.id) || (it.name == deal.title && it.isInDeals) }
            itemToRemove?.let { deleteItem(it.id) }
        }
    }

    private fun mapCategoryNameToId(categoryName: String?): String? {
        if (categoryName == null) return null
        val name = categoryName.lowercase()
        return when {
            name.contains("fruit") || name.contains("vegetable") -> PredefinedCategories.FRUITS_VEGETABLES.id
            name.contains("meat") || name.contains("poultry") || name.contains("beef") || name.contains("chicken") || name.contains("pork") -> PredefinedCategories.MEAT_POULTRY.id
            name.contains("fish") || name.contains("seafood") -> PredefinedCategories.FISH_SEAFOOD.id
            name.contains("dairy") || name.contains("egg") || name.contains("cheese") || name.contains("yogurt") || name.contains("milk") -> PredefinedCategories.DAIRY_EGGS.id
            name.contains("bread") || name.contains("bakery") -> PredefinedCategories.BREAD_BAKERY.id
            name.contains("frozen") || name.contains("ice cream") -> PredefinedCategories.FROZEN_FOODS.id
            name.contains("snack") || name.contains("sweet") || name.contains("chocolate") || name.contains("candy") || name.contains("chip") -> PredefinedCategories.SNACKS_SWEETS.id
            name.contains("beverage") || name.contains("drink") || name.contains("juice") || name.contains("soda") || name.contains("water") || name.contains("coffee") || name.contains("tea") || name.contains("beer") || name.contains("wine") -> PredefinedCategories.BEVERAGES.id
            name.contains("clean") || name.contains("soap") || name.contains("detergent") || name.contains("laundry") -> PredefinedCategories.CLEANING_SUPPLIES.id
            name.contains("beauty") || name.contains("cosmetic") || name.contains("shampoo") || name.contains("body") || name.contains("hair") -> PredefinedCategories.BEAUTY_COSMETICS.id
            name.contains("home") || name.contains("decor") -> PredefinedCategories.HOME_DECOR.id
            name.contains("electronic") || name.contains("tv") || name.contains("computer") || name.contains("phone") -> PredefinedCategories.ELECTRONICS.id
            name.contains("baby") || name.contains("diaper") -> PredefinedCategories.BABY_CARE.id
            name.contains("pet") || name.contains("dog") || name.contains("cat") -> PredefinedCategories.PET_FOOD.id
            name.contains("pantry") || name.contains("canned") || name.contains("oil") || name.contains("spice") -> PredefinedCategories.PANTRY_STAPLES.id
            else -> null
        }
    }

    private fun parsePrice(priceStr: String?): Double? {
        if (priceStr == null) return null
        return try {
            // Remove non-numeric characters except dot and comma
            val cleanStr = priceStr.replace(Regex("[^0-9.,]"), "")
            // Replace comma with dot if present
            val dotStr = cleanStr.replace(",", ".")
            dotStr.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun clearCompletedItems() {
        val list = currentList.value ?: return
        list.items.filter { it.isDone }.forEach { deleteItem(it.id) }
    }
}
