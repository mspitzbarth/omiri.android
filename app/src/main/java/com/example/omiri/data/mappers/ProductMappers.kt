package com.example.omiri.data.mappers

import androidx.compose.ui.graphics.Color
import com.example.omiri.data.api.models.ProductResponse
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Mapper functions to convert API models to UI models
 */

/**
 * Convert ProductResponse to Deal model for UI display
 */
fun ProductResponse.toDeal(): Deal {
    val discountLabel = if (hasDiscount && discountPercentage != null) {
        "${discountPercentage.roundToInt()}% OFF"
    } else null
    
    val timeLeftLabel = calculateTimeLeft(availableUntil)
    
    val priceString = if (priceAmount != null && priceCurrency != null) {
        formatPrice(priceAmount, priceCurrency)
    } else {
        "Price N/A"
    }
    
    val originalPriceString = if (originalPrice != null && priceCurrency != null) {
        formatPrice(originalPrice, priceCurrency)
    } else null
    
    return Deal(
        id = id.toString(),
        title = title,
        store = retailer ?: "Unknown Store",
        price = priceString,
        originalPrice = originalPriceString,
        discountLabel = discountLabel,
        timeLeftLabel = timeLeftLabel,
        category = categories?.firstOrNull() ?: "General",
        brand = brand,
        description = description,
        country = country,
        zipcode = zipcode,
        availableFrom = availableFrom,
        availableUntil = availableUntil,
        isFavorite = false,
        heroColor = getHeroColor(categories?.firstOrNull(), id.toString()),
        discountPercentage = discountPercentage?.roundToInt() ?: 0,
        hasDiscount = hasDiscount,
        isOnShoppingList = false, // Default, updated by ViewModel if needed
        searchTerm = searchTerm, // Map search term
        pdfSourceUrl = pdfSourceUrl,
        imageUrl = productImageUrl
    )
}

/**
 * Convert list of ProductResponse to list of Deal
 */
fun List<ProductResponse>.toDeals(): List<Deal> {
    return this.map { it.toDeal() }
}

/**
 * Format price with currency symbol
 */
private fun formatPrice(amount: Double, currency: String): String {
    return when (currency.uppercase()) {
        "USD" -> "$${String.format("%.2f", amount)}"
        "EUR" -> "€${String.format("%.2f", amount)}"
        "GBP" -> "£${String.format("%.2f", amount)}"
        else -> "${String.format("%.2f", amount)} $currency"
    }
}

/**
 * Calculate time left label from available until date
 */
private fun calculateTimeLeft(availableUntil: String?): String? {
    if (availableUntil == null) return null
    
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val endDate = dateFormat.parse(availableUntil) ?: return null
        val now = Date()
        
        val diffInMillis = endDate.time - now.time
        if (diffInMillis <= 0) return "Expired"
        
        val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((diffInMillis / (1000 * 60 * 60)) % 24).toInt()
        
        return when {
            days > 7 -> "${days} days left"
            days > 0 -> "${days} day${if (days > 1) "s" else ""} left"
            hours > 0 -> "${hours} hour${if (hours > 1) "s" else ""} left"
            else -> "Ending soon"
        }
    } catch (e: Exception) {
        return null
    }
}

/**
 * Get color for category using AppColors palettes
 */
private fun getHeroColor(category: String?, id: String): Color {
    val cat = category?.lowercase() ?: "general"
    
    // Palettes based on dummy data logic
    val electronicsPalette = listOf(AppColors.HeroBlue, AppColors.HeroSlate, AppColors.HeroViolet)
    val accessoriesPalette = listOf(AppColors.HeroSand, AppColors.HeroAqua)
    val cleaningPalette = listOf(AppColors.HeroCream, AppColors.HeroMint, AppColors.HeroIce, AppColors.HeroOrange, AppColors.HeroBlueSoft, AppColors.HeroLemon)
    val outdoorsPalette = listOf(AppColors.HeroForest)
    val foodPalette = listOf(AppColors.HeroMint, AppColors.HeroOrange, AppColors.HeroLemon, AppColors.HeroCream)
    
    // Default palette combining all
    val defaultPalette = listOf(
        AppColors.HeroBlue, AppColors.HeroSand, AppColors.HeroSlate, 
        AppColors.HeroViolet, AppColors.HeroForest, AppColors.HeroAqua,
        AppColors.HeroCream, AppColors.HeroMint, AppColors.HeroIce,
        AppColors.HeroOrange, AppColors.HeroLemon, AppColors.HeroBlueSoft
    )

    val palette = when {
        cat.contains("electronic") || cat.contains("tech") -> electronicsPalette
        cat.contains("accessor") -> accessoriesPalette
        cat.contains("cleaning") || cat.contains("home") -> cleaningPalette
        cat.contains("outdoor") || cat.contains("garden") -> outdoorsPalette
        cat.contains("food") || cat.contains("grocer") || cat.contains("snack") -> foodPalette
        else -> defaultPalette
    }
    
    val index = Math.abs(id.hashCode()) % palette.size
    return palette[index]
}
