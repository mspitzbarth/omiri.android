package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun FeaturedRecipeCard(
    modifier: Modifier = Modifier,
    title: String = "Creamy Chicken Pasta",
    description: String = "A rich and comforting pasta dish with tender chicken in a creamy sauce",
    time: String = "30 min",
    servings: String = "4 servings",
    difficulty: String = "Medium",
    rating: Double = 4.8,
    reviews: Int = 234,
    costPerServing: String = "€3.20",
    onViewRecipeClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(AppColors.Neutral200)
            ) {
                // "Chef's Pick" Badge
                Surface(
                    modifier = Modifier.padding(Spacing.sm),
                    color = AppColors.BrandOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Chef's Pick",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Button
                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.sm)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Add to favorites", tint = AppColors.Neutral600)
                }
            }

            // Content Section
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecipeTag(label = "Quick & Easy", color = Color(0xFFFFF7ED), textColor = AppColors.BrandOrange)
                    RecipeTag(label = "Budget-Friendly", color = Color(0xFFF0FDF4), textColor = AppColors.Success)
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Neutral600,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Stats Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecipeStatItem(icon = Icons.Outlined.Schedule, label = time)
                    RecipeStatItem(icon = Icons.Outlined.Group, label = servings)
                    RecipeStatItem(icon = Icons.Outlined.Star, label = "$rating ($reviews)", iconTint = Color(0xFFFFB800))
                }

                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(AppColors.Neutral100))
                Spacer(modifier = Modifier.height(Spacing.sm))

                // Bottom row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cost per serving", style = MaterialTheme.typography.labelSmall, color = AppColors.SubtleText)
                        Text(costPerServing, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppColors.BrandOrange)
                    }

                    Button(
                        onClick = onViewRecipeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Recipe", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeTag(label: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun RecipeStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, iconTint: Color = AppColors.Neutral400) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, size = 16.dp, tint = iconTint)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = AppColors.Neutral600)
    }
}

@Composable
private fun Icon(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}
