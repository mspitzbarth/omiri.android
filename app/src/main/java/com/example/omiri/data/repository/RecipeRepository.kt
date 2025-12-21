package com.example.omiri.data.repository

import com.example.omiri.data.api.RetrofitClient
import com.example.omiri.data.api.models.RecipeDataWrapper
import com.example.omiri.data.api.models.RecipeGenerateRequest
import com.example.omiri.data.api.models.RecipeListResponse
import com.example.omiri.data.api.models.RecipeResponse
import com.example.omiri.data.api.services.RecipeApiService

/**
 * Repository for recipe-related API calls.
 */
class RecipeRepository {

    private val apiService: RecipeApiService = RetrofitClient.createService()

    /**
     * Generate a recipe via AI.
     */
    suspend fun generateRecipe(
        ingredients: List<String>,
        preferences: String? = null,
        language: String? = "en",
        includeShoppingList: Boolean = false
    ): Result<RecipeResponse> {
        return try {
            val response = apiService.generateRecipe(RecipeGenerateRequest(ingredients, preferences, language, includeShoppingList))
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val recipe = body.recipe
                if (recipe != null) {
                    Result.success(recipe)
                } else {
                    Result.failure(Exception("Recipe data missing in response"))
                }
            } else {
                val errorMsg = body?.message ?: "Recipe generation failed (Code: ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * List recipes with filters.
     */
    suspend fun listRecipes(
        query: String? = null,
        ingredients: List<String>? = null,
        categories: List<String>? = null,
        dishTypes: List<String>? = null,
        language: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<RecipeResponse>> {
        return try {
            val response = apiService.listRecipes(query, ingredients, categories, dishTypes, language, limit, offset)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Result.success(body.recipes ?: emptyList())
            } else {
                val errorMsg = body?.message ?: "Listing recipes failed (Code: ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get details of a specific recipe.
     */
    suspend fun getRecipeDetails(recipeId: Int): Result<RecipeResponse> {
        return try {
            val response = apiService.getRecipeDetails(recipeId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                val recipe = body.recipe
                if (recipe != null) {
                    Result.success(recipe)
                } else {
                    Result.failure(Exception("Recipe details missing in response"))
                }
            } else {
                val errorMsg = body?.message ?: "Getting recipe details failed (Code: ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
