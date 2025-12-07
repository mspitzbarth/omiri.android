package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.data.api.models.StoreResponse
import com.example.omiri.data.api.models.StoresByRetailerResponse
import retrofit2.http.*

/**
 * API service for store-related endpoints
 */
interface StoreApiService {
    
    /**
     * Get stores list (grouped by retailer+country)
     * GET /stores
     */
    @GET("stores")
    suspend fun getStores(
        @Query("country") country: String? = null,
        @Query("retailer") retailer: String? = null,
        @Query("zipcode") zipcode: String? = null,
        @Query("city") city: String? = null,
        @Query("limit") limit: Int = 250,
        @Query("page") page: Int = 1
    ): List<StoreListResponse>
    
    /**
     * Get store by ID
     * GET /stores/{store_id}
     */
    @GET("stores/{store_id}")
    suspend fun getStoreById(
        @Path("store_id") storeId: Int
    ): StoreResponse
    
    /**
     * Get all stores for a retailer and country
     * GET /stores/by-retailer/{retailer}/{country}
     */
    @GET("stores/by-retailer/{retailer}/{country}")
    suspend fun getStoresByRetailer(
        @Path("retailer") retailer: String,
        @Path("country") country: String,
        @Query("active_only") activeOnly: Boolean = true
    ): StoresByRetailerResponse
    
    /**
     * Get store logo by store ID
     * GET /stores/{store_id}/logo
     */
    @GET("stores/{store_id}/logo")
    suspend fun getStoreLogo(
        @Path("store_id") storeId: Int
    ): okhttp3.ResponseBody
    
    /**
     * Get logo by filename
     * GET /logos/{filename}
     */
    @GET("logos/{filename}")
    suspend fun getLogoByFilename(
        @Path("filename") filename: String
    ): okhttp3.ResponseBody
    
    /**
     * List all available logos
     * GET /logos
     */
    @GET("logos")
    suspend fun listLogos(): List<String>
}
