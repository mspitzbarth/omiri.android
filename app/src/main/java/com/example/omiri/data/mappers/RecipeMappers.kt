package com.example.omiri.data.mappers

import com.example.omiri.data.api.models.RecipeResponse
import com.example.omiri.ui.components.RecipeMiniData

/**
 * Maps RecipeResponse from API to RecipeMiniData used in UI.
 */
fun RecipeResponse.toMiniData(): RecipeMiniData {
    return RecipeMiniData(
        id = this.id,
        title = this.recipeName,
        description = this.description,
        time = this.timeEstimate.total ?: "unknown",
        rating = 4.5, // Placeholder as backend doesn't provide rating yet
        price = "€${String.format("%.2f", (this.ingredientsList.size) * 0.85)}", // Rough estimate based on ingredient count
        imageUrl = this.imageUrl,
        matchPercentage = null // Will be calculated based on shopping list later
    )
}

/**
 * Maps a list of RecipeResponse to a list of RecipeMiniData.
 */
fun List<RecipeResponse>.toMiniDataList(): List<RecipeMiniData> {
    return this.map { it.toMiniData() }
}
