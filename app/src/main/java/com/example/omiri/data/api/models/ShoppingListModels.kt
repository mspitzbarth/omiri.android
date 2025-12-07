package com.example.omiri.data.api.models

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
