package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun RecipeCategoryGrid(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {}
) {
    val categories = listOf(
        CategoryData("Meat", Icons.Default.SetMeal, Color(0xFFEF4444)), // Red
        CategoryData("Vegetarian", Icons.Default.Eco, Color(0xFF22C55E)), // Green
        CategoryData("Seafood", Icons.Default.Sailing, Color(0xFF3B82F6)), // Blue
        CategoryData("Breakfast", Icons.Default.BreakfastDining, Color(0xFFEAB308)), // Yellow
        CategoryData("Asian", Icons.Default.RiceBowl, Color(0xFFF97316)), // Orange
        CategoryData("Italian", Icons.Default.LocalPizza, Color(0xFFA855F7)), // Purple
        CategoryData("Desserts", Icons.Default.Cake, Color(0xFFEC4899)), // Pink
        CategoryData("Soups", Icons.Default.SoupKitchen, Color(0xFF14B8A6)) // Teal
    )

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
        Text(
            text = "Browse by Category",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.BrandInk
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        // Using a fixed height or a flow layout since it's inside a scrollable screen
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val rows = categories.chunked(4)
            rows.forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowCategories.forEach { category ->
                        CategoryItem(category, onClick = { onCategoryClick(category.name) })
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(data: CategoryData, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(data.color, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(data.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = data.name,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.Neutral600,
            fontWeight = FontWeight.Medium
        )
    }
}

data class CategoryData(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
