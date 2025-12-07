package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.R
import com.example.omiri.ui.components.DealsCarousel
import com.example.omiri.ui.components.OmiriHeader
import com.example.omiri.ui.components.SectionHeader

import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ProductViewModel

@Composable
private fun ShoppingListPreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "Quick List Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "Add items from deals and organize them into lists.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("Groceries") }
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Household") }
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Next Week") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateAllDeals: () -> Unit = {},
    onDealClick: (String) -> Unit = {},
    fixedHeader: Boolean = true,
    onNotificationsClick: () -> Unit = {},
    onToggleShoppingList: (com.example.omiri.data.models.Deal, Boolean) -> Unit = { _, _ -> },
    viewModel: ProductViewModel = viewModel()
) {
    // Collect state from ViewModel
    val featuredDeals by viewModel.featuredDeals.collectAsState()
    val shoppingListDeals by viewModel.shoppingListDeals.collectAsState()
    val leavingSoonDeals by viewModel.leavingSoonDeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    // AdMob Interstitial
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { com.example.omiri.ui.components.InterstitialAdManager(context) }
    
    LaunchedEffect(Unit) {
        viewModel.initialLoad()
        viewModel.loadCategoriesIfNeeded()
        adManager.loadAd()
    }

    // Process Categories into Pills
    val categoryPills = remember(categories) {
        categories.map { catId ->
            com.example.omiri.ui.components.CategoryPill(
                id = catId,
                name = catId.replace("_", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString() } },
                emoji = com.example.omiri.util.EmojiHelper.getCategoryEmoji(catId).ifEmpty { "🏷️" }
            )
        }
    }
    
    // Pull to refresh
    @OptIn(ExperimentalMaterial3Api::class)
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    if (isRefreshing) {
        LaunchedEffect(Unit) {
            adManager.showAd {
                viewModel.loadProducts()
                isRefreshing = false
                adManager.loadAd() // Load next one
            }
        }
    }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = { isRefreshing = true },
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header (Fixed or Scrollable handling)
            // Note: Simplification - OmiriHeader is fixed at top usually in this design.
            // We will keep the fixed header pattern as per request (fixedHeader param).
            
            if (fixedHeader) {
                Column(
                     modifier = Modifier.background(Color(0xFFF9FAFB)) // Overall background Light Gray
                ) {
                    OmiriHeader(
                        notificationCount = 2,
                        onNotificationClick = onNotificationsClick
                    )
                    // Search Bar fixed below header? Or scrollable?
                    // Plan said scrollable at top. Let's put it in the scrollable part.
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF9FAFB)) // Ensure background covers search area
                        .verticalScroll(rememberScrollState())
                ) {
                     if (!fixedHeader) {
                        OmiriHeader(
                            notificationCount = 2,
                            onNotificationClick = onNotificationsClick
                        )
                    }
                    
                    // SEARCH BAR
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
                        com.example.omiri.ui.components.HomeSearchBar(
                            onSearchClick = onNavigateAllDeals // Navigate to search/all deals
                        )
                    }
                    
                    // HERO SECTION (First 5 Featured)
                    if (featuredDeals.isNotEmpty()) {
                        com.example.omiri.ui.components.HomeHeroCarousel(
                            deals = featuredDeals.take(5),
                            onDealClick = onDealClick
                        )
                    }
                    
                    // CATEGORIES
                    if (categoryPills.isNotEmpty()) {
                        com.example.omiri.ui.components.CategoryPillsRow(
                            categories = categoryPills,
                            onCategoryClick = { /* Navigate to category */ onNavigateAllDeals() } // TODO: Pass category filter
                        )
                        Spacer(Modifier.height(Spacing.xl))
                    }
                    
                    // SAVINGS DASHBOARD
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                         // Connect real data correctly
                         val potentialSavings by viewModel.shoppingListSavings.collectAsState(initial = 0.0)
                         com.example.omiri.ui.components.SavingsDashboard(potentialSavings = potentialSavings)
                         Spacer(Modifier.height(Spacing.xl))
                    }

                    // --- Section: More Daily Drops (Remaining Featured) ---
                    if (featuredDeals.size > 5) {
                         Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                            SectionHeader(
                                title = "More Daily Drops",
                                actionText = "View All",
                                onActionClick = onNavigateAllDeals
                            )
                            Spacer(Modifier.height(Spacing.md))
                        }
                        DealsCarousel(
                            deals = featuredDeals.drop(5),
                            onDealClick = onDealClick,
                            onToggleShoppingList = onToggleShoppingList
                        )
                         Spacer(Modifier.height(Spacing.xl))
                    }

                    // --- Section: Shopping List ---
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    ) {
                        SectionHeader(
                            title = "Your Shopping List",
                            actionText = "View All",
                            onActionClick = onNavigateAllDeals
                        )
                        Spacer(Modifier.height(Spacing.md))
                    }

                    // Shopping list deals from API
                    DealsCarousel(
                        deals = shoppingListDeals,
                        onDealClick = onDealClick,
                        onToggleShoppingList = onToggleShoppingList
                    )
                    
                    Spacer(Modifier.height(Spacing.md))
                    
                    // Shopping List Widget
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                         ShoppingListPreviewCard()
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // --- Section: Leaving Soon ---
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    ) {
                        SectionHeader(
                            title = "Leaving soon",
                            actionText = "View All",
                            onActionClick = onNavigateAllDeals
                        )

                        Spacer(Modifier.height(Spacing.md))
                    }

                    // Leaving soon deals from API
                    DealsCarousel(
                        deals = leavingSoonDeals,
                        onDealClick = onDealClick,
                        onToggleShoppingList = onToggleShoppingList
                    )

                    Spacer(Modifier.height(Spacing.xxxl))
                }
                
                // Loading Overlay
                if (isLoading && featuredDeals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFEA580C) // Orange
                        )
                    }
                }
            }
        }
    }
}
