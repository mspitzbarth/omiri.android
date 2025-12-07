package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.CategoryResponse
import retrofit2.http.*

/**
 * API service for category endpoints
 */
interface CategoryApiService {
    
    /**
     * Get all unique product categories with counts and translations
     * GET /categories
     */
    @GET("categories")
    suspend fun getCategories(
        @Query("retailer") retailer: String? = null,
        @Query("country") country: String? = null,
        @Query("zipcode") zipcode: String? = null,
        @Query("min_products") minProducts: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int = 1
    ): List<CategoryResponse>
}
