package com.example.omiri.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for generating a recipe.
 */
data class RecipeGenerateRequest(
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("preferences") val preferences: String? = null,
    @SerializedName("language") val language: String? = "en",
    @SerializedName("include_shopping_list") val includeShoppingList: Boolean = false
)

/**
 * Model for time estimates.
 */
data class RecipeTimeEstimate(
    @SerializedName("prep") val prep: String? = null,
    @SerializedName("cook") val cook: String? = null,
    @SerializedName("total") val total: String? = null
)

/**
 * Model for nutritional profile.
 */
data class RecipeNutritionalProfile(
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("protein_g") val proteinG: Float? = null,
    @SerializedName("carbs_g") val carbsG: Float? = null,
    @SerializedName("fat_g") val fatG: Float? = null
)

data class RecipeMetadata(
    @SerializedName("speed_category") val speedCategory: String? = null,
    @SerializedName("budget_category") val budgetCategory: String? = null,
    @SerializedName("skill_level") val skillLevel: String? = null,
    @SerializedName("cleanup_level") val cleanupLevel: String? = null,
    @SerializedName("meal_type") val mealType: String? = null,
    @SerializedName("cuisine") val cuisine: String? = null
)

/**
 * Model for an ingredient in a recipe.
 */
data class RecipeIngredient(
    @SerializedName("item") val item: String,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("is_staple") val isStaple: Boolean = false,
    @SerializedName("is_missing") val isMissing: Boolean = false,
    @SerializedName("user_provided") val userProvided: Boolean = true,
    @SerializedName("price_info") val priceInfo: IngredientPriceInfo? = null
)

data class RecipeShoppingList(
    @SerializedName("is_needed") val isNeeded: Boolean = false,
    @SerializedName("items") val items: List<String> = emptyList()
)

data class IngredientPriceInfo(
    @SerializedName("price") val price: Double,
    @SerializedName("retailer") val retailer: String,
    @SerializedName("store_city") val storeCity: String? = null,
    @SerializedName("product_id") val productId: Int? = null
)

/**
 * Response model for a single recipe.
 */
data class RecipeResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("recipe_name") val recipeName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("dietary_tags") val dietaryTags: List<String> = emptyList(),
    @SerializedName("allergen_warnings") val allergenWarnings: List<String> = emptyList(),
    @SerializedName("time_estimate") val timeEstimate: RecipeTimeEstimate,
    @SerializedName("ingredients_list") val ingredientsList: List<RecipeIngredient> = emptyList(),
    @SerializedName("instructions") val instructions: List<String> = emptyList(),
    @SerializedName("nutritional_per_serving") val nutritionalProfile: RecipeNutritionalProfile? = null,
    @SerializedName("chef_tip") val chefTip: String? = null,
    @SerializedName("servings") val servings: Int? = null,
    @SerializedName("metadata") val metadata: RecipeMetadata? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("dish_type") val dishType: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("shopping_list") val shoppingList: RecipeShoppingList? = null,
    @SerializedName("language") val language: String? = "en",
    @SerializedName("created_at") val createdAt: String
)

/**
 * Response model for recipe list.
 */
data class RecipeListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int,
    @SerializedName("recipes") val recipes: List<RecipeResponse> = emptyList(),
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * General wrapper for recipe response (used in generate endpoint).
 */
data class RecipeDataWrapper(
    @SerializedName("success") val success: Boolean,
    @SerializedName("recipe_id") val recipeId: Int? = null,
    @SerializedName("recipe") val recipe: RecipeResponse? = null,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("message") val message: String? = null
)
