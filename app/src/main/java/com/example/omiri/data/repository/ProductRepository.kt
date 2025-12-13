package com.example.omiri.data.repository

import com.example.omiri.data.api.RetrofitClient
import com.example.omiri.data.api.models.ProductResponse
import com.example.omiri.data.api.models.ProductsResponse
import com.example.omiri.data.api.models.ProductSearchRequest
import com.example.omiri.data.api.services.ProductApiService

/**
 * Repository for product-related API calls
 * 
 * This is an example repository showing how to use the API services.
 * Handles data transformation and error handling.
 */
class ProductRepository {
    
    private val apiService: ProductApiService = RetrofitClient.createService()
    
    /**
     * Get products with optional filters
     * Returns ProductsResponse with pagination metadata from headers
     */
    suspend fun getProducts(
        country: String? = null,
        retailer: String? = null,
        retailers: String? = null,
        categories: String? = null,
        zipcode: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        hasDiscount: Boolean? = null,
        availableFromMin: String? = null,
        availableFromMax: String? = null,
        availableUntilMin: String? = null,
        availableUntilMax: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        activeOnly: Boolean? = true,
        limit: Int = 25,
        page: Int = 1
    ): Result<ProductsResponse> {
        return try {
            val response = apiService.getProducts(
                country = country,
                retailer = retailer,
                retailers = retailers,
                categories = categories,
                zipcode = zipcode,
                minPrice = minPrice,
                maxPrice = maxPrice,
                hasDiscount = hasDiscount,
                activeOnly = activeOnly,
                availableFromMin = availableFromMin,
                availableFromMax = availableFromMax,
                availableUntilMin = availableUntilMin,
                availableUntilMax = availableUntilMax,
                sortBy = sortBy,
                sortOrder = sortOrder,
                limit = limit,
                page = page
            )
            
            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                val headers = response.headers()
                
                // Extract pagination metadata from headers
                val pageNum = headers["x-page"]?.toIntOrNull() ?: page
                val totalCount = headers["x-total-count"]?.toIntOrNull() ?: 0
                val totalPages = headers["x-total-pages"]?.toIntOrNull() ?: 1
                val hasMore = headers["x-has-more"]?.toBoolean() ?: false
                
                val productsResponse = ProductsResponse(
                    products = products,
                    page = pageNum,
                    totalCount = totalCount,
                    totalPages = totalPages,
                    hasMore = hasMore
                )
                
                Result.success(productsResponse)
            } else {
                Result.failure(Exception("Search failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Get categories failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search products by query
     */
    suspend fun searchProducts(
        query: String,
        retailer: String? = null,
        zipcode: String? = null
    ): Result<Map<String, List<ProductResponse>>> {
        return try {
            val response = apiService.searchProducts(
                query = query,
                retailer = retailer,
                zipcode = zipcode
            )
            // Transform results map<String, Result> to map<String, List<Product>>
            val transformed = response.results.mapValues { it.value.products }
            Result.success(transformed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get product by ID
     */
    suspend fun getProductById(productId: String): Result<ProductResponse> {
        return try {
            val product = apiService.getProductById(productId)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search by barcode
     */
    suspend fun searchByBarcode(barcode: String): Result<List<ProductResponse>> {
        return try {
            val products = apiService.searchByBarcode(barcode)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get autocomplete suggestions
     */
    suspend fun getAutocomplete(query: String, limit: Int = 10): Result<List<String>> {
        return try {
            val suggestions = apiService.getAutocomplete(query, limit)
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Find similar products
     */
    suspend fun findSimilar(productId: String, limit: Int = 10): Result<List<ProductResponse>> {
        return try {
            val products = apiService.findSimilar(productId, limit)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Find best price for a product
     */
    suspend fun findBestPrice(
        productName: String,
        zipcode: String? = null
    ): Result<List<ProductResponse>> {
        return try {
            val products = apiService.findBestPrice(productName, zipcode)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Search shopping list items
     */
    /**
     * Search shopping list items
     */
    suspend fun searchShoppingList(
        items: String,
        country: String? = null,
        retailers: String? = null,
        stores: String? = null,
        storeGroupIds: String? = null,
        zipcode: String? = null,
        activeOnly: Boolean? = null,
        excludeExpired: Boolean? = null,
        limit: Int? = null
    ): Result<com.example.omiri.data.api.models.ShoppingListSearchResponse> {
        return try {
            val response = apiService.searchShoppingList(
                items = items,
                country = country,
                retailers = retailers,
                stores = stores,
                storeGroupIds = storeGroupIds,
                zipcode = zipcode,
                activeOnly = activeOnly,
                excludeExpired = excludeExpired,
                limit = limit
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Search shopping list failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun optimizeShoppingList(
        userZipcode: String? = null,
        maxStores: Int = 3
    ): Result<com.example.omiri.data.api.models.ShoppingListOptimizeResponse> {
        return try {
            val request = com.example.omiri.data.api.models.ShoppingListOptimizeRequest(
                userZipcode = userZipcode,
                maxStores = maxStores
            )
            val response = apiService.optimizeShoppingList(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Optimization failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getProductsBulk(ids: List<String>): Result<List<ProductResponse>> {
        return try {
            val request = com.example.omiri.data.api.models.BulkProductRequest(ids)
            val response = apiService.getProductsBulk(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Bulk fetch failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppSync(
        country: String? = null, 
        zipcode: String? = null,
        stores: String? = null,
        activeOnly: Boolean? = true
    ): Result<com.example.omiri.data.api.models.AppSyncResponse> {
         return try {
            val response = apiService.getAppSync(country, zipcode, stores, activeOnly)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
