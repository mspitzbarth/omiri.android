package com.example.omiri.data.repository

import com.example.omiri.data.models.PredefinedCategories
import com.example.omiri.data.models.ShoppingItem
import com.example.omiri.data.models.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Singleton repository for managing shopping lists.
 * In a real app, this would use Room database or DataStore.
 * For now, it holds in-memory state shared across ViewModels.
 */
object ShoppingListRepository {

    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId: StateFlow<String?> = _currentListId.asStateFlow()

    init {
        createDefaultList()
    }

    private fun createDefaultList() {
        // Start with an empty list
        val defaultList = ShoppingList(
            id = UUID.randomUUID().toString(),
            name = "My List",
            items = emptyList() // Empty!
        )

        _shoppingLists.value = listOf(defaultList)
        _currentListId.value = defaultList.id
    }

    fun createList(name: String) {
        val newList = ShoppingList(
            id = UUID.randomUUID().toString(),
            name = name
        )
        _shoppingLists.update { lists ->
            lists + newList
        }
        _currentListId.value = newList.id
    }

    fun switchList(listId: String) {
        _currentListId.value = listId
    }

    fun deleteList(listId: String) {
        _shoppingLists.update { lists ->
            val updatedLists = lists.filter { it.id != listId }
            if (_currentListId.value == listId && updatedLists.isNotEmpty()) {
                _currentListId.value = updatedLists.first().id
            }
            updatedLists
        }
    }

    fun renameList(listId: String, newName: String) {
        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(name = newName)
                } else {
                    list
                }
            }
        }
    }

    fun addItem(name: String, categoryId: String = PredefinedCategories.OTHER.id, isInDeals: Boolean = false, isRecurring: Boolean = false) {
        val listId = _currentListId.value ?: return

        val newItem = ShoppingItem(
            id = UUID.randomUUID().toString(),
            name = name,
            isInDeals = isInDeals,
            categoryId = categoryId,
            isRecurring = isRecurring
        )

        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(items = list.items + newItem)
                } else {
                    list
                }
            }
        }
    }

    fun updateItem(itemId: String, name: String, categoryId: String, isRecurring: Boolean) {
        val listId = _currentListId.value ?: return

        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(
                        items = list.items.map { item ->
                            if (item.id == itemId) {
                                item.copy(name = name, categoryId = categoryId, isRecurring = isRecurring)
                            } else {
                                item
                            }
                        }
                    )
                } else {
                    list
                }
            }
        }
    }

    fun toggleItemDone(itemId: String) {
        val listId = _currentListId.value ?: return

        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(
                        items = list.items.map { item ->
                            if (item.id == itemId) {
                                item.copy(isDone = !item.isDone)
                            } else {
                                item
                            }
                        }
                    )
                } else {
                    list
                }
            }
        }
    }

    fun deleteItem(itemId: String) {
        val listId = _currentListId.value ?: return

        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(items = list.items.filter { it.id != itemId })
                } else {
                    list
                }
            }
        }
    }
    
    fun setItemInDeals(itemId: String, isInDeals: Boolean) {
        val listId = _currentListId.value ?: return
        
        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    list.copy(
                        items = list.items.map { item ->
                            if (item.id == itemId) {
                                item.copy(isInDeals = isInDeals)
                            } else {
                                item
                            }
                        }
                    )
                } else {
                    list
                }
            }
        }
    }
    
    fun resetRecurringItems() {
        val listId = _currentListId.value ?: return

        _shoppingLists.update { lists ->
            lists.map { list ->
                if (list.id == listId) {
                    val newItems = list.items.mapNotNull { item ->
                        if (item.isRecurring) {
                            item.copy(isDone = false)
                        } else {
                            if (item.isDone) null else item
                        }
                    }
                    list.copy(items = newItems)
                } else {
                    list
                }
            }
        }
    }
    
    // New method for API support (clearing lists etc)
    fun updateShoppingLists(newLists: List<ShoppingList>) {
        _shoppingLists.value = newLists
    }
}
