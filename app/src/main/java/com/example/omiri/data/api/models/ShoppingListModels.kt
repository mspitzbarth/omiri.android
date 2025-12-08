package com.example.omiri.data.api.models

import com.example.omiri.data.api.models.ProductResponse
import com.google.gson.annotations.SerializedName

/**
 * Shopping list response
 */
data class ShoppingListResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("items") val items: List<ShoppingListItemResponse>?
)

/**
 * Shopping list item response
 */
data class ShoppingListItemResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("list_id") val listId: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("notes") val notes: String?,
    @SerializedName("is_checked") val isChecked: Boolean = false,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

/**
 * Request to create a shopping list
 */
data class ShoppingListCreateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("notes") val notes: String?
)

/**
 * Request to add item to shopping list
 */
data class ShoppingListItemAddRequest(
    @SerializedName("product_name") val productName: String,
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("notes") val notes: String?
)

/**
 * Request to update shopping list item
 */
data class ShoppingListItemUpdateRequest(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("is_checked") val isChecked: Boolean?
)

/**
 * Request to optimize shopping list
 */
data class ShoppingListOptimizeRequest(
    @SerializedName("user_zipcode") val userZipcode: String?,
    @SerializedName("max_stores") val maxStores: Int = 3
)

/**
 * Response for shopping list search
 */
/**
 * Response for shopping list search
 */
data class ShoppingListSearchResponse(
    @SerializedName("shopping_list") val shoppingList: List<String> = emptyList(),
    @SerializedName("total_items") val totalItems: Int = 0,
    @SerializedName("items_found") val itemsFound: Int = 0,
    @SerializedName("items_not_found") val itemsNotFound: List<String> = emptyList(),
    @SerializedName("categories") val categories: Map<String, CategoryResult>? = emptyMap()
)

data class CategoryResult(
    @SerializedName("items") val items: List<String> = emptyList(),
    @SerializedName("product_count") val productCount: Int = 0,
    @SerializedName("products") val products: List<ProductResponse> = emptyList()
)

data class ShoppingListOptimizeResponse(
    @SerializedName("total_savings") val totalSavings: Double,
    @SerializedName("original_price") val originalPrice: Double,
    @SerializedName("optimized_price") val optimizedPrice: Double,
    @SerializedName("steps") val steps: List<OptimizationStep>
)

data class OptimizationStep(
    @SerializedName("store_name") val storeName: String,
    @SerializedName("store_color") val storeColor: String? = null, // e.g. blue for Lidl
    @SerializedName("items_count") val itemsCount: Int,
    @SerializedName("items") val items: List<String>,
    @SerializedName("step_savings") val stepSavings: Double,
    @SerializedName("total_cost") val totalCost: Double
)
