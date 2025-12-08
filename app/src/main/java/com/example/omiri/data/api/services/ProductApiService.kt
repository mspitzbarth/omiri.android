package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.ProductResponse
import com.example.omiri.data.api.models.ProductSearchRequest
import retrofit2.http.*

/**
 * API service for product-related endpoints
 */
interface ProductApiService {
    
    /**
     * Get products with filters
     * GET /products
     * Returns Response to access pagination headers
     */
    @GET("products")
    suspend fun getProducts(
        @Query("country") country: String? = null,
        @Query("retailer") retailer: String? = null,
        @Query("retailers") retailers: String? = null,
        @Query("search") search: String? = null,
        @Query("categories") categories: String? = null,
        @Query("zipcode") zipcode: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("has_discount") hasDiscount: Boolean? = null,
        @Query("online_only") onlineOnly: Boolean? = null,
        @Query("active_only") activeOnly: Boolean? = true,
        @Query("active_from_min") availableFromMin: String? = null,
        @Query("active_from_max") availableFromMax: String? = null,
        @Query("available_until_min") availableUntilMin: String? = null,
        @Query("available_until_max") availableUntilMax: String? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("page") page: Int = 1
    ): retrofit2.Response<List<ProductResponse>>
    
    /**
     * Search products for shopping list
     * GET /products/search
     */
    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("categories") categories: String? = null,
        @Query("retailer") retailer: String? = null,
        @Query("country") country: String? = null,
        @Query("zipcode") zipcode: String? = null,
        @Query("limit_per_item") limitPerItem: Int = 5,
        @Query("active_only") activeOnly: Boolean = true
    ): Map<String, List<ProductResponse>>
    
    /**
     * Get product by ID
     * GET /products/{product_id}
     */
    @GET("products/{product_id}")
    suspend fun getProductById(
        @Path("product_id") productId: String
    ): ProductResponse
    
    /**
     * Advanced product search with fuzzy matching (API v1)
     * POST /api/v1/search/products
     */
    @POST("api/v1/search/products")
    suspend fun searchProductsAdvanced(
        @Body request: ProductSearchRequest
    ): List<ProductResponse>
    
    /**
     * Search by barcode (API v1)
     * GET /api/v1/search/barcode/{barcode}
     */
    @GET("api/v1/search/barcode/{barcode}")
    suspend fun searchByBarcode(
        @Path("barcode") barcode: String
    ): List<ProductResponse>
    
    /**
     * Get autocomplete suggestions (API v1)
     * GET /api/v1/search/autocomplete
     */
    @GET("api/v1/search/autocomplete")
    suspend fun getAutocomplete(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): List<String>
    
    /**
     * Find similar products (API v1)
     * GET /api/v1/search/similar/{product_id}
     */
    @GET("api/v1/search/similar/{product_id}")
    suspend fun findSimilar(
        @Path("product_id") productId: String,
        @Query("limit") limit: Int = 10
    ): List<ProductResponse>
    
    /**
     * Find best price for a product (API v1)
     * GET /api/v1/match/best-price
     */
    @GET("api/v1/match/best-price")
    suspend fun findBestPrice(
        @Query("product_name") productName: String,
        @Query("zipcode") zipcode: String? = null
    ): List<ProductResponse>

    /**
     * Search products for shopping list and group by category
     * GET /shopping-list/search
     */
    /**
     * Search shopping list items and check for deals
     * GET /shopping-list/search
     */
    @GET("shopping-list/search")
    suspend fun searchShoppingList(
        @Query("items") items: String,
        @Query("country") country: String? = null,
        @Query("retailers") retailers: String? = null,
        @Query("stores") stores: String? = null,
        @Query("store_group_ids") storeGroupIds: String? = null,
        @Query("zipcode") zipcode: String? = null,
        @Query("active_only") activeOnly: Boolean? = null,
        @Query("exclude_expired") excludeExpired: Boolean? = null,
        @Query("limit") limit: Int? = null
    ): retrofit2.Response<com.example.omiri.data.api.models.ShoppingListSearchResponse>

    /**
     * Get all categories
     * GET /categories
     */
    @GET("categories")
    suspend fun getCategories(): retrofit2.Response<List<String>>

    /**
     * Optimize shopping list
     * POST /shopping-list/optimize
     */
    @POST("shopping-list/optimize")
    suspend fun optimizeShoppingList(
        @Body request: com.example.omiri.data.api.models.ShoppingListOptimizeRequest
    ): retrofit2.Response<com.example.omiri.data.api.models.ShoppingListOptimizeResponse>
}
