package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.*
import retrofit2.http.*

/**
 * API service for shopping list endpoints (API v1)
 */
interface ShoppingListApiService {
    
    /**
     * Create a new shopping list
     * POST /api/v1/shopping-lists
     */
    @POST("api/v1/shopping-lists")
    suspend fun createShoppingList(
        @Body request: ShoppingListCreateRequest
    ): ShoppingListResponse
    
    /**
     * Get all shopping lists for a user
     * GET /api/v1/shopping-lists
     */
    @GET("api/v1/shopping-lists")
    suspend fun getUserShoppingLists(
        @Query("user_id") userId: String,
        @Query("active_only") activeOnly: Boolean = true
    ): List<ShoppingListResponse>
    
    /**
     * Get a shopping list with all items
     * GET /api/v1/shopping-lists/{list_id}
     */
    @GET("api/v1/shopping-lists/{list_id}")
    suspend fun getShoppingList(
        @Path("list_id") listId: Int
    ): ShoppingListResponse
    
    /**
     * Delete a shopping list
     * DELETE /api/v1/shopping-lists/{list_id}
     */
    @DELETE("api/v1/shopping-lists/{list_id}")
    suspend fun deleteShoppingList(
        @Path("list_id") listId: Int
    ): Map<String, Any>
    
    /**
     * Add an item to a shopping list
     * POST /api/v1/shopping-lists/{list_id}/items
     */
    @POST("api/v1/shopping-lists/{list_id}/items")
    suspend fun addItem(
        @Path("list_id") listId: Int,
        @Body request: ShoppingListItemAddRequest
    ): ShoppingListItemResponse
    
    /**
     * Update a shopping list item
     * PUT /api/v1/shopping-lists/items/{item_id}
     */
    @PUT("api/v1/shopping-lists/items/{item_id}")
    suspend fun updateItem(
        @Path("item_id") itemId: Int,
        @Body request: ShoppingListItemUpdateRequest
    ): ShoppingListItemResponse
    
    /**
     * Delete a shopping list item
     * DELETE /api/v1/shopping-lists/items/{item_id}
     */
    @DELETE("api/v1/shopping-lists/items/{item_id}")
    suspend fun deleteItem(
        @Path("item_id") itemId: Int
    ): Map<String, Any>
    

}
