package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Store response from API
 */
data class StoreResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("retailer") val retailer: String,
    @SerializedName("country") val country: String,
    @SerializedName("store_name") val storeName: String?,
    @SerializedName("zipcode") val zipcode: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?,
    @SerializedName("locale") val locale: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("store_code") val storeCode: String?,
    @SerializedName("store_page_url") val storePageUrl: String?,
    @SerializedName("logo_path") val logoPath: String?,
    @SerializedName("has_location_specific_flyers") val hasLocationSpecificFlyers: Boolean = false,
    @SerializedName("check_interval_hours") val checkIntervalHours: Int = 24,
    @SerializedName("source_job_id") val sourceJobId: Int?,
    @SerializedName("active") val active: Boolean = true,
    @SerializedName("last_check_at") val lastCheckAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("has_multiple_locations") val hasMultipleLocations: Boolean = false,
    @SerializedName("zipcodes") val zipcodes: List<String>?
)

/**
 * Store list response (grouped by retailer+country)
 */
data class StoreListResponse(
    @SerializedName("id") val id: String,
    @SerializedName("retailer") val retailer: String,
    @SerializedName("country") val country: String,
    @SerializedName("store_count") val storeCount: Int,
    @SerializedName("active_count") val activeCount: Int,
    @SerializedName("has_multiple_locations") val hasMultipleLocations: Boolean,
    @SerializedName("store_ids") val storeIds: List<Int>,
    @SerializedName("zipcodes") val zipcodes: List<String>,
    @SerializedName("sample_store_names") val sampleStoreNames: List<String>?
)

/**
 * Response wrapper for stores by retailer endpoint
 */
data class StoresByRetailerResponse(
    @SerializedName("stores") val stores: List<StoreResponse>? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("limit") val limit: Int? = null
)
