package com.example.omiri.data.models

data class ShoppingItem(
    val id: String,
    val name: String,
    val isDone: Boolean = false,
    val isInDeals: Boolean = false,
    val categoryId: String = PredefinedCategories.OTHER.id,
    val isRecurring: Boolean = false,
    val dealId: String? = null, // Reference to the deal if item is on sale
    val addedAt: Long = System.currentTimeMillis()
) {
    val category: ShoppingCategory
        get() = PredefinedCategories.getCategoryById(categoryId)
}
