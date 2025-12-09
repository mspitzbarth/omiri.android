package com.example.omiri.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.R
import com.example.omiri.ui.components.*
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.SettingsViewModel
import com.example.omiri.viewmodels.ProductViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.CloudOff

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllDealsScreen(
    title: String = "All Deals",
    onDealClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onToggleShoppingList: (com.example.omiri.data.models.Deal, Boolean) -> Unit = { _, _ -> },
    onNavigateToMyStores: () -> Unit = {},
    viewModel: ProductViewModel,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var showFilterModal by remember { mutableStateOf(false) }
    val allStores by settingsViewModel.allStores.collectAsState()
    val myStoreIds by settingsViewModel.selectedStoreIds.collectAsState()
    
    // Filter allStores to only show what is in myStoreIds (User's active/saved stores)
    val myStoresList = remember(allStores, myStoreIds) {
        allStores.filter { myStoreIds.contains(it.id) }
    }

    var currentFilters by remember { mutableStateOf(FilterOptions()) }
    var currentPage by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Collect state from ViewModel
    val allDeals by viewModel.allDeals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPaging by viewModel.isPaging.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    // AdMob Interstitial
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { com.example.omiri.ui.components.InterstitialAdManager(context) }
    
    LaunchedEffect(Unit) {
        viewModel.initialLoad()
        adManager.loadAd()
    }

    // Filter State
    var selectedFilterChip by remember { mutableStateOf("This Week") }
    var toggledFilterChips by remember { mutableStateOf(emptySet<String>()) }
    
    // Logic to update ViewModel
    LaunchedEffect(selectedFilterChip, toggledFilterChips) {
        val mode = when {
             toggledFilterChips.contains("My Deals") -> "MY_DEALS"
             selectedFilterChip == "Next Week" -> "NEXT_WEEK"
             else -> "THIS_WEEK"
        }
        viewModel.setFilterMode(mode)
    }

    // Determine which deals to show based on filter
    val shoppingListDeals by viewModel.shoppingListDeals.collectAsState()
    val isMyDeals = toggledFilterChips.contains("My Deals")
    
    // Use shoppingListDeals if My Deals is toggled, otherwise allDeals. 
    val currentDeals = if (isMyDeals) shoppingListDeals else allDeals
    
    // Group deals by category for the list view
    val groupedDeals = remember(currentDeals) { 
        currentDeals.groupBy { it.category ?: "Other" } 
    }

    // Pull to refresh
    @OptIn(ExperimentalMaterial3Api::class)
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    if (isRefreshing) {
        LaunchedEffect(Unit) {
            adManager.showAd {
                scope.launch {
                    viewModel.refreshProducts()
                    isRefreshing = false
                    adManager.loadAd()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fixed Header Section
        OmiriHeader(
            notificationCount = 2,
            onNotificationClick = onNotificationsClick,
            onProfileClick = onProfileClick
        )
        
        // Interstitial Ad Logic: Show every 200 items
        LaunchedEffect(allDeals.size) {
             if (allDeals.size > 0 && allDeals.size % 200 == 0) {
                 adManager.showAd {}
             }
        }

        // Search and Filters (Fixed)
        Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
            Spacer(Modifier.height(Spacing.sm))
            OmiriSearchBar()
            Spacer(Modifier.height(Spacing.md))
            
            // Filters & Sort Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter button (on the left) with badge
                val activeFilterCount = listOf(
                    currentFilters.priceRange != 0f..1000f,
                    currentFilters.selectedStores.isNotEmpty(),
                    currentFilters.selectedCategories.isNotEmpty(),
                    currentFilters.onlineOnly
                ).count { it }

                val hasActiveFilters = activeFilterCount > 0

                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFE8357),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = activeFilterCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { showFilterModal = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "More filters",
                            tint = if (hasActiveFilters) Color(0xFFFE8357) else Color(0xFF1F2937)
                        )
                    }
                }

                MixedFilterChipsRow(
                    modifier = Modifier.weight(1f),
                    options = listOf(
                        MixedFilterOption("My Deals", Icons.Outlined.BookmarkBorder, isToggleable = true),
                        MixedFilterOption("This Week", Icons.Outlined.Today, isToggleable = false),
                        MixedFilterOption("Next Week", Icons.Outlined.Event, isToggleable = false)
                    ),
                    initialSelected = selectedFilterChip,
                    initialToggled = toggledFilterChips,
                    onSelectedChange = { selectedFilterChip = it },
                    onToggledChange = { toggledFilterChips = it },
                    selectedBackgroundColor = Color(0xFFFE6B36),
                    selectedTextColor = Color.White,
                    unselectedBackgroundColor = Color(0xFFF3F4F6),
                    unselectedTextColor = Color.Black
                )
            }
            
            Spacer(Modifier.height(Spacing.lg))
        }



        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.weight(1f) // Take remaining space
        ) {
            if (isLoading && !isPaging) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                            com.example.omiri.ui.components.OmiriLoader()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search/Filters moved out
                    
                    // Track cumulative items for ad insertion
            var cumulativeItemCount = 0
            var justInsertedAd = false
            
            // Feed Items - Grouped by Category (Vertical Layout)
            groupedDeals.forEach { (category, deals) ->
                item(key = "header_$category") {
                    Text(
                        text = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier
                            .padding(horizontal = Spacing.lg)
                            .padding(top = Spacing.lg, bottom = Spacing.sm)
                    )
                }
                
                // Vertical Grid Logic: Chunk into rows of 2
                val chunkedDeals = deals.chunked(2)
                
                chunkedDeals.forEach { rowDeals ->
                    item(key = "${category}_row_${rowDeals.first().id}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            // First Item
                            DealCard(
                                deal = rowDeals[0],
                                onClick = { onDealClick(rowDeals[0].id) },
                                onFavoriteChange = { d, fav -> onToggleShoppingList(d, fav) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Second Item (if exists)
                            if (rowDeals.size > 1) {
                                DealCard(
                                    deal = rowDeals[1],
                                    onClick = { onDealClick(rowDeals[1].id) },
                                    onFavoriteChange = { d, fav -> onToggleShoppingList(d, fav) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Spacer for grid alignment
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    
                    val previousCount = cumulativeItemCount
                    cumulativeItemCount += rowDeals.size
                    justInsertedAd = false
                    
                    // Insert ad every 12 items (whenever we cross a multiple of 12 boundary)
                    if (cumulativeItemCount / 12 > previousCount / 12) {
                        item(key = "ad_banner_${category}_${rowDeals.first().id}") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.md)
                                    .heightIn(min = 60.dp), // Ensure minimal height
                                contentAlignment = Alignment.Center
                            ) {
                                AdMobBanner()
                            }
                        }
                        justInsertedAd = true
                    }
                }
            }
            
            // Empty State & Initial Loading logic (kept)
            if (allDeals.isEmpty()) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.omiri.ui.components.OmiriLoader(size = 48.dp)
                        }
                    }
                } else {
                     item {
                        // Smart Empty State
                        val error by viewModel.error.collectAsState()
                        val networkErrorType by viewModel.networkErrorType.collectAsState()
                        val emptyMessage = "Try adjusting your filters or search query"
                        
                        com.example.omiri.ui.components.OmiriSmartEmptyState(
                            networkErrorType = networkErrorType,
                            error = error,
                            onRetry = { viewModel.loadProducts() },
                            defaultIcon = androidx.compose.material.icons.Icons.Outlined.LocalOffer,
                            defaultTitle = "No deals found",
                            defaultMessage = emptyMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xxl)
                        )
                     }
                }
            }
            
            // Ad Gatekeeper for Infinite Scroll
            if (hasMore) {
                if (justInsertedAd) {
                    // We just showed an ad, so don't show another gatekeeper ad immediately.
                    // Just trigger the load with a small indicator/spacer.
                    item(key = "ad_gatekeeper_loader") {
                         LaunchedEffect(Unit) {
                             if (!isLoading) {
                                 viewModel.loadMoreDeals()
                             }
                         }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.md),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.omiri.ui.components.OmiriLoader(size = 24.dp)
                        }
                    }
                } else {
                    // Show Gatekeeper Ad which triggers load
                    item(key = "ad_gatekeeper") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.md),
                            contentAlignment = Alignment.Center
                        ) {
                            AdMobBanner(
                                 onAdLoaded = {
                                     if (!isLoading) {
                                         scope.launch {
                                             delay(1000) 
                                             viewModel.loadMoreDeals()
                                         }
                                     }
                                 }
                            )
                        }
                    }
                }
                
                 if (isLoading && !justInsertedAd) {
                    item(key = "loading_spinner") {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xl)) {
                                    com.example.omiri.ui.components.OmiriLoader()
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(Spacing.xxxl))
            }
        }
        }
    }
    }
    
    // Filter Modal Logic (Update apply to set viewModel params)
    // Collect filter data
    val availableCategories by viewModel.categories.collectAsState()
    val availableStores by viewModel.availableStores.collectAsState()

    LaunchedEffect(showFilterModal) {
        if (showFilterModal) {
            viewModel.loadCategoriesIfNeeded()
        }
    }

    FilterModal(
        isVisible = showFilterModal,
        onDismiss = { showFilterModal = false },
        onApply = { filters ->
            currentFilters = filters
            viewModel.applyFilters(
                priceRange = filters.priceRange,
                sortBy = filters.sortBy,
                sortOrder = filters.sortOrder,
                hasDiscount = filters.hasDiscount
            )
        },
        initialFilters = currentFilters,
        availableCategories = availableCategories,
        availableStores = availableStores
    )
}

// Sealed interfaces for data and rows
sealed interface FeedItem {
    data class DealItem(val deal: com.example.omiri.data.models.Deal) : FeedItem
    data class AdItem(val id: String) : FeedItem
}

sealed interface FeedItemRow {
    data class DealRow(val deals: List<Pair<com.example.omiri.data.models.Deal, androidx.compose.ui.graphics.Color>>) : FeedItemRow
    data class AdRow(val id: String) : FeedItemRow
}

data class CategoryFilterOption(
    val id: String,
    val name: String,
    val emoji: String
)

@Composable
fun CategoriesSwipeFilterRow(
    categories: List<CategoryFilterOption>,
    selectedCategoryIds: Set<String>,
    onCategoryToggle: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = 2.dp) // align with parent padding if passed, but parent has padding.
        // Parent container has horizontal padding? 
        // In the usage site: Column(modifier = Modifier.padding(horizontal = Spacing.lg))
        // So the LazyRow is inside padding.
        // It's better if LazyRow is edge-to-edge and has contentPadding.
        // But the usage site wraps it in a padded Column. 
        // I'll stick to simple implementation matching the context.
    ) {
        items(categories) { category ->
            val isSelected = selectedCategoryIds.contains(category.id)
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryToggle(category.id) },
                label = {
                    Text(
                        text = "${category.emoji} ${category.name}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFE8357),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF1F2937)
                ),
                border = FilterChipDefaults.filterChipBorder(
                     enabled = true,
                     selected = isSelected,
                     borderColor = if (isSelected) Color(0xFFFE8357) else Color(0xFFE5E7EB)
                )
            )
        }
    }
}
