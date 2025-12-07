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

    init {
        // Sync open shopping list items to UserPreferences for background worker
        android.util.Log.d("ShoppingListViewModel", "Initializing ShoppingListViewModel sync")
        viewModelScope.launch {
            shoppingLists.collect { lists ->
                val allItems = lists.flatMap { it.items }
                    .filter { !it.isDone } // Only active items
                    .map { it.name }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(",")
                
                userPreferences.saveShoppingListItems(allItems)
            }
        }
    }

    // Actions delegate to Repository
    fun createList(name: String) = repository.createList(name)
    fun switchList(listId: String) = repository.switchList(listId)
    fun deleteList(listId: String) = repository.deleteList(listId)
    fun renameList(listId: String, newName: String) = repository.renameList(listId, newName)
    
    fun addItem(name: String, categoryId: String = PredefinedCategories.OTHER.id, isInDeals: Boolean = false, isRecurring: Boolean = false) {
        repository.addItem(name, categoryId, isInDeals, isRecurring)
    }

    fun toggleItemDone(itemId: String) = repository.toggleItemDone(itemId)
    fun deleteItem(itemId: String) = repository.deleteItem(itemId)
    fun resetRecurringItems() = repository.resetRecurringItems()

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
