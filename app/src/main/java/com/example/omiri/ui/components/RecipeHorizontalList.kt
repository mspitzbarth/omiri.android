package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun RecipeHorizontalList(
    modifier: Modifier = Modifier,
    title: String,
    recipes: List<RecipeMiniData>,
    showViewAll: Boolean = true,
    showMatchBadge: Boolean = false,
    onViewAllClick: () -> Unit = {},
    onRecipeClick: (RecipeMiniData) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandInk
                )
                if (showMatchBadge) {
                   Text(
                        text = "Recipes using items you're buying",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500
                    )
                }
            }
            if (showViewAll) {
                Text(
                    text = "View All",
                    modifier = Modifier.clickable { onViewAllClick() },
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = Spacing.md)
        ) {
            items(recipes) { recipe ->
                RecipeMiniCard(
                    data = recipe,
                    showMatchBadge = showMatchBadge,
                    onClick = { onRecipeClick(recipe) }
                )
            }
        }
    }
}

@Composable
fun RecipeMiniCard(
    data: RecipeMiniData,
    showMatchBadge: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(AppColors.Neutral200)
            ) {
                if (!data.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = data.imageUrl,
                        contentDescription = data.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (showMatchBadge && data.matchPercentage != null) {
                    Surface(
                        modifier = Modifier.padding(8.dp),
                        color = AppColors.Success,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${data.matchPercentage}% Match",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, size = 16.dp, tint = AppColors.Neutral900)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Neutral900,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                if (data.description != null) {
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Neutral500,
                        maxLines = 2,
                        minLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Star, contentDescription = null, size = 14.dp, tint = Color(0xFFFFB800))
                    Text(
                        text = " ${data.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Neutral600,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${data.time}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Neutral600
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = data.price,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandOrange
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AppColors.BrandOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }
    }
}

data class RecipeMiniData(
    val id: Int,
    val title: String,
    val description: String? = null,
    val time: String,
    val rating: Double,
    val price: String,
    val imageUrl: String? = null,
    val matchPercentage: Int? = null
)

@Composable
private fun Icon(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}
