package com.example.omiri.data.models

data class ShoppingList(
    val id: String,
    val name: String,
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalItems: Int
        get() = items.size

    val doneItems: Int
        get() = items.count { it.isDone }

    val toBuyItems: Int
        get() = items.count { !it.isDone }

    val dealItems: Int
        get() = items.count { it.isInDeals }
}
