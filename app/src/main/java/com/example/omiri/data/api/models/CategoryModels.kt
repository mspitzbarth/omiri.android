package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Category response with count and translations
 */
data class CategoryResponse(
    @SerializedName("category") val category: String,
    @SerializedName("count") val count: Int,
    @SerializedName("translations") val translations: Map<String, String>?
)
