package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Product response from API matching ProductResponse schema
 */
data class ProductResponse(
    @SerializedName("id") val id: String,
    @SerializedName("pdf_id") val pdfId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("price_amount") val priceAmount: Double?,
    @SerializedName("price_currency") val priceCurrency: String?,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("discount_percentage") val discountPercentage: Double?,
    @SerializedName("has_discount") val hasDiscount: Boolean,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("badges") val badges: List<String>?,
    @SerializedName("page_number") val pageNumber: Int?,
    @SerializedName("confidence") val confidence: Double?,
    @SerializedName("retailer") val retailer: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("zipcode") val zipcode: String?,
    @SerializedName("online_only") val onlineOnly: Boolean?,
    @SerializedName("featured") val featured: Boolean? = false,
    @SerializedName("available_from") val availableFrom: String?,
    @SerializedName("available_until") val availableUntil: String?,
    @SerializedName("availability_text") val availabilityText: String?,
    @SerializedName("pdf_source_url") val pdfSourceUrl: String?,
    @SerializedName("product_image_url") val productImageUrl: String?,
    @SerializedName("search_term") val searchTerm: String? = null
)

/**
 * Product search request for API v1
 */
data class ProductSearchRequest(
    @SerializedName("query") val query: String?,
    @SerializedName("fuzzy") val fuzzy: Boolean = false,
    @SerializedName("fuzzy_threshold") val fuzzyThreshold: Int = 80,
    @SerializedName("retailers") val retailers: List<String>?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("min_price") val minPrice: Double?,
    @SerializedName("max_price") val maxPrice: Double?,
    @SerializedName("has_discount") val hasDiscount: Boolean?,
    @SerializedName("discount_min") val discountMin: Double?,
    @SerializedName("limit") val limit: Int = 50,
    @SerializedName("offset") val offset: Int = 0
)

/**
 * Paginated response wrapper
 */
data class PaginatedResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("total") val total: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("has_more") val hasMore: Boolean?
)

/**
 * Products response with pagination metadata from headers
 */
data class ProductsResponse(
    val products: List<ProductResponse>,
    val page: Int,
    val totalCount: Int,
    val totalPages: Int,
    val hasMore: Boolean
)

data class SmartAlert(
    val title: String,
    val type: String, // "PRICE_DROP", "CHEAPEST", "EXPIRING", "INFO"
    val iconName: String = "INFO" // For UI mapping
)

/**
 * Request for bulk product fetch
 * POST /api/v1/products/bulk
 */
data class BulkProductRequest(
    @SerializedName("ids") val ids: List<String>
)

/**
 * Response for App Sync
 * GET /api/v1/app/sync
 */
data class AppSyncResponse(
    @SerializedName("featured_deals") val featuredDeals: List<ProductResponse>,
    @SerializedName("top_deals") val topDeals: List<ProductResponse>?,
    @SerializedName("expiring_soon") val expiringSoon: List<ProductResponse>?,
    @SerializedName("stores") val stores: List<StoreListResponse>?, 
    @SerializedName("categories") val categories: List<CategoryResponse>?,
    @SerializedName("config") val config: Map<String, Any>?
)

/**
 * Response for product search
 */
data class ProductSearchResponse(
    @SerializedName("search_items") val searchItems: List<String>,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("results") val results: Map<String, ProductSearchResult>
)

data class ProductSearchResult(
    @SerializedName("count") val count: Int,
    @SerializedName("products") val products: List<ProductResponse>
)
