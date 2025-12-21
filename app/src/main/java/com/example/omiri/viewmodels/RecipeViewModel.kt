package com.example.omiri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.omiri.data.api.models.RecipeResponse
import com.example.omiri.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing recipes.
 * Handles AI generation, searching, and viewing recipe details.
 */
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository()

    // UI State
    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<RecipeResponse?>(null)
    val selectedRecipe: StateFlow<RecipeResponse?> = _selectedRecipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val TAG = "RecipeViewModel"

    /**
     * Search recipes based on ingredients and filters.
     */
    fun searchRecipes(
        query: String? = null,
        ingredients: List<String>? = null,
        categories: List<String>? = null,
        dishTypes: List<String>? = null,
        language: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.listRecipes(query, ingredients, categories, dishTypes, language)
                .onSuccess { recipeList ->
                    _recipes.value = recipeList
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to search recipes", e)
                    _error.value = "Failed to load recipes: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    /**
     * Generate a new recipe using AI.
     */
    fun generateRecipe(ingredients: List<String>, preferences: String? = null, language: String? = "en", includeShoppingList: Boolean = false) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            
            repository.generateRecipe(ingredients, preferences, language, includeShoppingList)
                .onSuccess { newRecipe ->
                    _selectedRecipe.value = newRecipe
                    // Optionally refresh the list
                    searchRecipes()
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to generate recipe", e)
                    _error.value = "AI Generation failed: ${e.message}"
                }
            
            _isGenerating.value = false
        }
    }

    /**
     * Load details for a specific recipe.
     */
    fun loadRecipeDetails(recipeId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getRecipeDetails(recipeId)
                .onSuccess { recipe ->
                    _selectedRecipe.value = recipe
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load recipe details", e)
                    _error.value = "Failed to load recipe details: ${e.message}"
                }
            
            _isLoading.value = false
        }
    }

    /**
     * Clear the selected recipe.
     */
    fun clearSelection() {
        _selectedRecipe.value = null
    }

    /**
     * Clear the error state.
     */
    fun clearError() {
        _error.value = null
    }
}
