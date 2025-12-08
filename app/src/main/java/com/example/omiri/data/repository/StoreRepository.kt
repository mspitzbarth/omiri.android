package com.example.omiri.data.repository

import com.example.omiri.data.api.RetrofitClient
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.data.api.models.StoreResponse
import com.example.omiri.data.api.services.StoreApiService

/**
 * Repository for store operations
 */
class StoreRepository {
    
    companion object {
        val instance by lazy { StoreRepository() }
    }

    private val apiService: StoreApiService = RetrofitClient.createService()
    
    /**
     * Get stores list (grouped by retailer+country)
     * 
     * @param country Filter by country code (e.g., "US", "DE")
     * @return Result containing list of stores or error
     */
    suspend fun getStores(
        country: String? = null
    ): Result<List<StoreListResponse>> {
        return try {
            val stores = apiService.getStores(country = country)
            Result.success(stores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all locations for a specific retailer and country
     * 
     * @param retailer Retailer name
     * @param country Country code
     * @return Result containing list of store locations or error
     */
    suspend fun getStoresByRetailer(
        retailer: String,
        country: String
    ): Result<List<StoreResponse>> {
        return try {
            val response = apiService.getStoresByRetailer(retailer, country)
            val stores = response.stores ?: emptyList()
            Result.success(stores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get store by ID
     * 
     * @param storeId Store ID
     * @return Result containing store details or error
     */
    suspend fun getStoreById(
        storeId: Int
    ): Result<StoreResponse> {
        return try {
            val store = apiService.getStoreById(storeId)
            Result.success(store)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
