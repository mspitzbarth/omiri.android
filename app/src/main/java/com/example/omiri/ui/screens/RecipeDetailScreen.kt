package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.RecipeViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBackClick: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val recipe by recipeViewModel.selectedRecipe.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()

    LaunchedEffect(recipeId) {
        recipeViewModel.loadRecipeDetails(recipeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.recipeName ?: "Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.BrandOrange)
            }
        } else if (recipe != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.md)
            ) {
                // Image Section
                if (!recipe!!.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = recipe!!.imageUrl,
                        contentDescription = recipe!!.recipeName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(AppColors.Neutral200, RoundedCornerShape(16.dp))
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                Text(
                    text = recipe!!.recipeName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandInk
                )

                recipe!!.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Neutral500,
                        modifier = Modifier.padding(vertical = Spacing.sm)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
                ) {
                    InfoItem(icon = Icons.Outlined.Schedule, label = recipe!!.timeEstimate.total ?: "unknown")
                    InfoItem(icon = Icons.Outlined.Group, label = "${recipe!!.servings ?: "4"} servings")
                    InfoItem(icon = Icons.Outlined.RestaurantMenu, label = "${recipe!!.nutritionalProfile?.calories ?: 0} kcal")
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // Metadata Chips
                recipe!!.metadata?.let { meta ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        meta.skillLevel?.let { SkillChip(it) }
                        meta.budgetCategory?.let { BudgetChip(it) }
                        meta.speedCategory?.let { SpeedChip(it) }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandInk
                )

                recipe!!.ingredientsList.forEach { ingredient ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ingredient.isMissing) {
                                    Surface(
                                        color = AppColors.BrandOrange.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = "NEED TO BUY",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.BrandOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = ingredient.item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (ingredient.isMissing) AppColors.BrandInk else AppColors.Neutral500
                                )
                            }
                            Text(
                                text = "${ingredient.amount ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandOrange
                            )
                        }
                        
                        // Show best price if missing
                        if (ingredient.isMissing && ingredient.priceInfo != null) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp, start = if (ingredient.isMissing) 80.dp else 0.dp) // Align after the badge if possible, but simpler is better
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Available at ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.Neutral500
                                )
                                Text(
                                    text = ingredient.priceInfo.retailer,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.BrandInk
                                )
                                Text(
                                    text = " for ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.Neutral500
                                )
                                Text(
                                    text = "€${String.format("%.2f", ingredient.priceInfo.price)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandInk
                )

                recipe!!.instructions.forEachIndexed { index, instruction ->
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BrandOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = instruction, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Allergen Warnings
                if (recipe!!.allergenWarnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.md)) {
                            Text(
                                text = "Allergen Warnings",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                            recipe!!.allergenWarnings.forEach { warning ->
                                Text(
                                    text = "• $warning",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkillChip(skill: String) {
    Surface(
        color = Color(0xFFE0F2FE),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = skill.replace("_", " ").capitalize(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF0369A1),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BudgetChip(budget: String) {
    Surface(
        color = Color(0xFFF0FDF4),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = budget.replace("_", " ").capitalize(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF15803D),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SpeedChip(speed: String) {
    Surface(
        color = Color(0xFFFDF2F8),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = speed.replace("_", " ").capitalize(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFBE185D),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, size = 18.dp, tint = AppColors.Neutral500)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = AppColors.Neutral500)
    }
}

@Composable
private fun Icon(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}
