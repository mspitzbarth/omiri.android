package com.example.omiri.util

import com.example.omiri.viewmodels.CategoryUiModel

object CategoryHelper {
    private var categories: List<CategoryUiModel> = emptyList()

    fun updateCategories(newCategories: List<CategoryUiModel>) {
        categories = newCategories
    }

    fun getCategoryName(id: String): String {
        return categories.find { it.id == id }?.name ?: id
    }
}
