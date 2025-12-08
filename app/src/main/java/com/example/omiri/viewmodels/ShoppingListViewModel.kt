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

    val filteredItems: StateFlow<List<ShoppingItem>> = combine(currentList, _searchQuery) { list, query ->
        val items = list?.items ?: emptyList()
        if (query.isNotEmpty()) {
            items.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            items
        }
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
            try {
                val list = currentList.value ?: return@launch
                val items = list.items.filter { !it.isDone }.map { it.name }.joinToString(",")
                
                if (items.isBlank()) return@launch
                
                val storesSet: Set<String> = userPreferences.selectedStores.firstOrNull() ?: emptySet()
                val stores: String? = if (storesSet.isNotEmpty()) storesSet.joinToString(",") else null
                
                val country = userPreferences.selectedCountry.firstOrNull() ?: "DE"
                
                // We can also pass other filters if we have them accessable
                val result = productRepository.searchShoppingList(
                    items = items,
                    stores = stores,
                    country = country,
                    limit = 3
                )
                
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

    // ... (rest of class) ...

    fun addItem(name: String, categoryId: String = PredefinedCategories.OTHER.id, isInDeals: Boolean = false, isRecurring: Boolean = false) {
        repository.addItem(name, categoryId, isInDeals, isRecurring)
        // Check deals for the new item (or refresh all)
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
