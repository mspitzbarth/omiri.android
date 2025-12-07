package com.example.omiri.data.models

import androidx.compose.ui.graphics.Color

data class Deal(
    val id: String,
    val title: String,
    val store: String,
    val price: String,
    val originalPrice: String? = null,
    val discountLabel: String? = null,
    val timeLeftLabel: String? = null,
    val category: String,
    val isFavorite: Boolean = false,
    val heroColor: Color? = null, // for placeholder images
    val imageUrl: String? = null,
    val discountPercentage: Int = 0,
    val hasDiscount: Boolean = false
)
