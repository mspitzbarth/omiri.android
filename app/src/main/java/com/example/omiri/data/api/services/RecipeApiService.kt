package com.example.omiri.data.api.services

import com.example.omiri.data.api.models.RecipeDataWrapper
import com.example.omiri.data.api.models.RecipeGenerateRequest
import com.example.omiri.data.api.models.RecipeListResponse
import com.example.omiri.data.api.models.RecipeResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API service for recipe-related endpoints.
 */
interface RecipeApiService {

    /**
     * Generate a recipe via AI.
     * POST /api/v1/recipes/generate
     */
    @POST("api/v1/recipes/generate")
    suspend fun generateRecipe(
        @Body request: RecipeGenerateRequest
    ): Response<RecipeDataWrapper>

    /**
     * List and search recipes with filters.
     * GET /api/v1/recipes
     */
    @GET("api/v1/recipes")
    suspend fun listRecipes(
        @Query("query") query: String? = null,
        @Query("ingredients") ingredients: List<String>? = null,
        @Query("categories") categories: List<String>? = null,
        @Query("dish_types") dishTypes: List<String>? = null,
        @Query("language") language: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<RecipeListResponse>

    /**
     * Get details of a specific recipe.
     * GET /api/v1/recipes/{recipe_id}
     */
    @GET("api/v1/recipes/{recipe_id}")
    suspend fun getRecipeDetails(
        @Path("recipe_id") recipeId: Int
    ): Response<RecipeDataWrapper>
}
