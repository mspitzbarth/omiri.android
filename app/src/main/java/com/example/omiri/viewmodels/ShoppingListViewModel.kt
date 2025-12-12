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
                val catName = PredefinedCategories.getCategoryById(catId).getName("en") // Default EN for now or pass Locale
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
    }

    val filteredItems: StateFlow<List<ShoppingItem>> = combine(currentList, _searchQuery, _selectedCategory, _filterItemNames) { list, query, categoryId, filterNames ->
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
        
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private fun checkDealsForCurrentList() {
        viewModelScope.launch {
            // Optimization: Move heavy checking to background thread to avoid UI stutter
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    val list = currentList.value ?: return@withContext
                    val items = list.items.filter { !it.isDone }.map { it.name }.joinToString(",")
                    
                    if (items.isBlank()) return@withContext
                    
                    val storesSet: Set<String> = userPreferences.selectedStores.firstOrNull() ?: emptySet()
                    val stores: String? = if (storesSet.isNotEmpty()) storesSet.joinToString(",") else null
                    
                    val country = userPreferences.selectedCountry.firstOrNull() ?: "DE"
                    
                    // IO Call
                    val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        productRepository.searchShoppingList(
                            items = items,
                            stores = stores,
                            country = country,
                            limit = 3
                        )
                    }
                    
                    result.onSuccess { response ->
                        // Defensive check for categories map
                        val categories = response.categories ?: emptyMap()
                        
                        // Flatten all found products to easily search by search_term
                        // Filter out any potential null values from map (though defined as non-null)
                        val allFoundProducts = categories.values.filterNotNull().flatMap { it.products }
                        
                        // Also track which items were successfully found according to API (mapped by category)
                        // But relying on products list is safer for "isInDeals" check.
                        
                        list.items.forEach { item ->
                            // Check if any product's search_term matches this item
                            val hasDeals = allFoundProducts.any { product ->
                                val term = product.searchTerm
                                // Fallback: fuzzy match on title if search_term missing? 
                                // The API provided search_term explicitly.
                                // We compare item.name with product.searchTerm
                                
                                // Strict check:
                                term.equals(item.name, ignoreCase = true) == true
                                
                                // Or looser check if item name is "Organic Milk" and term is "organic milk" -> match
                                // If item name "Milk" and product found for "Milk" -> match.
                            }
                            
                            if (item.isInDeals != hasDeals) {
                                repository.setItemInDeals(item.id, hasDeals)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ShoppingListViewModel", "Error checking deals: ${e.message}", e)
                }
            }
        }
    }

    // ... (rest of class) ...

    fun addItem(name: String, categoryId: String = PredefinedCategories.OTHER.id, isInDeals: Boolean = false, isRecurring: Boolean = false) {
        repository.addItem(name, categoryId, isInDeals, isRecurring)
        // Check deals for the new item (or refresh all)
        checkDealsForCurrentList()
    }

    fun updateItem(itemId: String, name: String, categoryId: String, isRecurring: Boolean) {
        repository.updateItem(itemId, name, categoryId, isRecurring)
        checkDealsForCurrentList()
    }

    fun toggleItemDone(itemId: String) = repository.toggleItemDone(itemId)
    fun deleteItem(itemId: String) = repository.deleteItem(itemId)
    fun resetRecurringItems() = repository.resetRecurringItems()
    
    fun createList(name: String) = repository.createList(name)
    fun switchList(listId: String) = repository.switchList(listId)
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
            val categoryId = when(deal.category) {
                "Food & Beverages" -> PredefinedCategories.FRUITS_VEGETABLES.id
                "Electronics" -> PredefinedCategories.OTHER.id
                else -> PredefinedCategories.OTHER.id
            }
            addItem(name = deal.title, categoryId = categoryId, isInDeals = true)
        } else {
            // Remove logic needs to be in Repository or implemented here using deleteItem
            // For now, simpler to implement removal logic in Repository if needed strictly,
            // but we can just loop through current list here since we have read access.
            // HOWEVER, modifying state must go through Repository.
            // Let's implement a 'removeDealItem' in Repository or just find ID here and call deleteItem.
            val list = currentList.value ?: return
            val itemToRemove = list.items.find { it.name == deal.title && it.isInDeals }
            itemToRemove?.let { deleteItem(it.id) }
        }
    }

    fun clearCompletedItems() {
        val list = currentList.value ?: return
        list.items.filter { it.isDone }.forEach { deleteItem(it.id) }
    }
}
