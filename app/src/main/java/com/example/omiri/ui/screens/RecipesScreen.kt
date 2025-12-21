package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.data.mappers.toMiniDataList
import com.example.omiri.viewmodels.RecipeViewModel
import com.example.omiri.ui.components.*
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip

@Composable
fun RecipesScreen(
    initialQuery: String? = null,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val recipes by recipeViewModel.recipes.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val error by recipeViewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }
    var ingredientQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (searchQuery.isNotEmpty()) {
            recipeViewModel.searchRecipes(query = searchQuery)
        } else {
            recipeViewModel.searchRecipes()
        }
    }
    // Nested Scroll Logic for Collapsible Header
    val density = androidx.compose.ui.platform.LocalDensity.current
    var filterBarHeightPx by remember { mutableFloatStateOf(0f) }
    var filterBarOffsetPx by remember { mutableFloatStateOf(0f) }
    var stickyAlertHeightPx by remember { mutableFloatStateOf(0f) }
    var showGenerationSheet by remember { mutableStateOf(false) }
    
    val isGenerating by recipeViewModel.isGenerating.collectAsState()
    val isSearchActive = searchQuery.isNotEmpty() || ingredientQuery.isNotEmpty()

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                val newOffset = (filterBarOffsetPx + delta).coerceIn(-filterBarHeightPx, 0f)
                filterBarOffsetPx = newOffset
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            OmiriHeader(
                onNotificationClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFFF9FAFB) // App Colors Bg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(nestedScrollConnection)
        ) {
            val filterBarHeightDp = with(density) { filterBarHeightPx.toDp() }
            val stickyAlertHeightDp = with(density) { stickyAlertHeightPx.toDp() }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                contentPadding = PaddingValues(top = filterBarHeightDp + stickyAlertHeightDp, bottom = Spacing.xxl)
            ) {
                if (!isSearchActive) {
                    // Regular Discovery View
                    // 5. Weekly Meal Planner Promo (High Prominence)
                    item {
                        WeeklyMealPlannerCard(
                            modifier = Modifier
                                .padding(horizontal = Spacing.md)
                                .padding(top = Spacing.sm),
                            onTryNowClick = { showGenerationSheet = true }
                        )
                    }

                    // 6. Featured Section
                    item {
                        Column(modifier = Modifier.padding(horizontal = Spacing.md)) {
                            Text(
                                text = "Featured This Week",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandInk
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                            val featuredRecipe = recipes.firstOrNull()
                            FeaturedRecipeCard(
                                title = featuredRecipe?.recipeName ?: "Creamy Chicken Pasta",
                                description = featuredRecipe?.description ?: "A rich and comforting pasta dish",
                                time = featuredRecipe?.timeEstimate?.total ?: "30 min",
                                imageUrl = featuredRecipe?.imageUrl,
                                onViewRecipeClick = { featuredRecipe?.id?.let { onRecipeClick(it) } }
                            )
                        }
                    }

                    // 7. Based on Your List
                    item {
                        RecipeHorizontalList(
                            title = "Based on Your List",
                            showMatchBadge = true,
                            recipes = recipes.filter { it.dishType == "Vegetarian" }.toMiniDataList(),
                            onRecipeClick = { miniData -> 
                                onRecipeClick(miniData.id)
                            }
                        )
                    }

                    // 8. Popular This Week
                    item {
                        RecipeHorizontalList(
                            title = "Popular This Week",
                            recipes = recipes.take(4).toMiniDataList(),
                            onRecipeClick = { miniData -> 
                                onRecipeClick(miniData.id)
                            }
                        )
                    }

                    // 9. Browse by Category
                    item {
                        RecipeCategoryGrid()
                    }

                    // 10. Trending Now
                    item {
                        Column(modifier = Modifier.padding(horizontal = Spacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Trending Now",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.BrandInk
                                    )
                                    Text(
                                        text = "What everyone's cooking",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.Neutral500
                                    )
                                }
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.BrandOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(Spacing.md))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TrendingRecipeRow(rank = 1, rankColor = Color(0xFFF97316), title = "One-Pot Pasta Primavera", views = "2.3k", rating = 4.9, price = "€3.40")
                                TrendingRecipeRow(rank = 2, rankColor = Color(0xFF94A3B8), title = "Crispy Baked Chicken Wings", views = "1.9k", rating = 4.8, price = "€4.90")
                                TrendingRecipeRow(rank = 3, rankColor = Color(0xFFB45309), title = "Chocolate Chip Cookies", views = "1.7k", rating = 5.0, price = "€2.20")
                            }
                        }
                    }

                    // 11. Savings Tip
                    item {
                        SavingsTipCard(
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                    }
                } else {
                    // Search Results View
                    item {
                        val resultText = if (searchQuery.isNotEmpty() && ingredientQuery.isNotEmpty()) {
                            "Results for \"$searchQuery\" with $ingredientQuery"
                        } else if (searchQuery.isNotEmpty()) {
                            "Results for \"$searchQuery\""
                        } else {
                            "Recipes with $ingredientQuery"
                        }
                        Text(
                            text = resultText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                    }

                    if (recipes.isEmpty() && !isLoading) {
                        item {
                            OmiriEmptyState(
                                title = "No recipes found",
                                message = "Try searching for something else or different ingredients."
                            )
                        }
                    } else {
                        items(recipes.size) { index ->
                            val recipe = recipes[index]
                            RecipeListItem(
                                modifier = Modifier
                                    .padding(horizontal = Spacing.md)
                                    .clickable { onRecipeClick(recipe.id) },
                                title = recipe.recipeName,
                                time = recipe.timeEstimate.total ?: "unknown",
                                servings = "4 servings", // Default servings for now
                                savedAmount = "€2.50",
                                store = "ALDI",
                                imageUrl = recipe.imageUrl
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(Spacing.xxl)) }
            }

            // Collapsible Header (Hero + Search + Chips) + Sticky Match Alert
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                // Part 1: Sliding Header (Hero + Search + Chips)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            filterBarHeightPx = coordinates.size.height.toFloat()
                        }
                        .graphicsLayer { translationY = filterBarOffsetPx }
                        .background(Color(0xFFF9FAFB)) // Match screen background
                ) {
                    /*
                    // 1. Hero Section
                    RecipeHero(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    )

                    */
                    // 2 & 3. Search + Chips Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(bottom = Spacing.lg)
                    ) {
                        // Recipe Name Search (Full width/prominent)
                        com.example.omiri.ui.components.OmiriSearchBar(
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                            placeholder = "Search recipes...",
                            value = searchQuery,
                            onQueryChange = { 
                                searchQuery = it
                                recipeViewModel.searchRecipes(query = it)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        
                        RecipeFilterChips()
                        
                        Spacer(modifier = Modifier.height(Spacing.md))
                        
                        // Bottom border
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                    }
                }

                /*
                // Part 2: Sticky Match Alert (Slides up with header but stays at top)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            stickyAlertHeightPx = coordinates.size.height.toFloat()
                        }
                        .graphicsLayer { translationY = filterBarOffsetPx }
                        .background(Color(0xFFF9FAFB))
                        .padding(bottom = Spacing.sm)
                ) {
                    RecipeMatchAlert(
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }

                 */
            }
        }

        if (showGenerationSheet) {
            RecipeGenerationBottomSheet(
                onDismiss = { showGenerationSheet = false },
                isGenerating = isGenerating,
                onGenerate = { ingredients, preferences, language, includeShoppingList ->
                    recipeViewModel.generateRecipe(ingredients, preferences, language, includeShoppingList)
                }
            )
        }

        // Handle success/selection navigation
        val selectedRecipe by recipeViewModel.selectedRecipe.collectAsState()
        LaunchedEffect(selectedRecipe) {
            if (selectedRecipe != null) {
                onRecipeClick(selectedRecipe!!.id)
                recipeViewModel.clearSelection()
                showGenerationSheet = false
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if(selected) Color(0xFFFE8357) else Color.White,
        border = if(!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if(selected) Color.White else Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecipeCard(
    title: String,
    time: String,
    servings: String,
    tags: List<String>,
    buttonText: String,
    width: androidx.compose.ui.unit.Dp,
    imageUrl: String? = null
) {
    Card(
        modifier = Modifier.width(width),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(140.dp)
                     .background(Color.Gray)
            ) {
                 if (!imageUrl.isNullOrEmpty()) {
                     AsyncImage(
                         model = imageUrl,
                         contentDescription = title,
                         modifier = Modifier.fillMaxSize(),
                         contentScale = ContentScale.Crop
                     )
                 }
                 
                 // Image placeholder
                 Surface(
                     modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                     color = Color(0xFFDCFCE7),
                     shape = RoundedCornerShape(4.dp)
                 ) {
                     Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                         // Check icon
                         Text("5 items", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                     }
                 }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Outlined.Group, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(servings, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if(tag == "Uses your list") Color(0xFFFE8357) else Color(0xFF166534))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun RecipeListItem(
    modifier: Modifier = Modifier,
    title: String,
    time: String,
    servings: String,
    savedAmount: String,
    store: String,
    imageUrl: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray, RoundedCornerShape(12.dp))
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.Group, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(servings, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFFFEDD5), shape = RoundedCornerShape(4.dp)) {
                        Text("Matched Deals", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFFE8357), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Save $savedAmount", style = MaterialTheme.typography.labelSmall, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text("Best at $store", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
