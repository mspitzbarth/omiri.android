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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.omiri.viewmodels.CategoryUiModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.outlined.LocalOffer
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllDealsScreen(
    title: String = "All Deals",
    initialQuery: String? = null,
    initialFilterMode: String? = null,
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
    // Remove unused page state if not needed, or keep for future
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
        if (initialQuery != null) {
            viewModel.searchProducts(initialQuery)
        } else {
            viewModel.refreshAllDeals()
        }
        adManager.loadAd()
    }

    // Filter State
    var selectedFilterChip by remember { mutableStateOf("This Week") }
    var toggledFilterChips by remember { 
        mutableStateOf(
            if (initialFilterMode == "MY_DEALS") setOf("My Deals") else emptySet<String>()
        )
    }
    
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

    // Nested Scroll Logic for Collapsible Header
    val density = androidx.compose.ui.platform.LocalDensity.current
    var filterBarHeightPx by remember { mutableFloatStateOf(0f) }
    var filterBarOffsetPx by remember { mutableFloatStateOf(0f) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Reactive Pagination Trigger
    LaunchedEffect(listState, hasMore, isLoading, isPaging) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItemsNumber = layoutInfo.totalItemsCount
                val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                
                if (lastVisibleItemIndex > (totalItemsNumber - 8) && hasMore && !isLoading && !isPaging && totalItemsNumber > 0) {
                    viewModel.loadMoreDeals()
                }
            }
    }
    
    // State to track last index where fullscreen ad was shown
    var lastAdShownIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex > 0 && firstVisibleIndex > lastAdShownIndex + 50) {
                    adManager.showAd {
                        lastAdShownIndex = firstVisibleIndex
                    }
                }
            }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.omiri.ui.theme.AppColors.Bg)
    ) {
        // 2. Content logic with PullToRefresh and Collapsible Filter Bar
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.weight(1f)
        ) {
            Box(
                 modifier = Modifier
                     .fillMaxSize()
                     .nestedScroll(nestedScrollConnection)
            ) {
                // List Content
                val filterBarHeightDp = with(density) { filterBarHeightPx.toDp() }
                
                if (isLoading && !isPaging && !isRefreshing) {
                    // Skeleton Grid State
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        contentPadding = PaddingValues(top = filterBarHeightDp + Spacing.md, bottom = Spacing.md, start = Spacing.lg, end = Spacing.lg)
                    ) {
                        items(6) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                                com.example.omiri.ui.components.DealCardSkeleton(modifier = Modifier.weight(1f))
                                com.example.omiri.ui.components.DealCardSkeleton(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = filterBarHeightDp, bottom = 0.dp)
                    ) {
                        var cumulativeItemCount = 0
                        var justInsertedAd = false
                        
                        groupedDeals.forEach { (category, deals) ->
                            item(key = "header_$category") {
                                Text(
                                    text = com.example.omiri.util.CategoryHelper.getCategoryName(category).replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827),
                                    modifier = Modifier
                                        .padding(horizontal = Spacing.lg)
                                        .padding(top = Spacing.lg, bottom = Spacing.sm)
                                )
                            }
                            
                            val chunkedDeals = deals.chunked(2)
                            chunkedDeals.forEach { rowDeals ->
                                item(key = "${category}_row_${rowDeals.first().id}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                                    ) {
                                        DealCard(
                                            deal = rowDeals[0],
                                            onClick = { onDealClick(rowDeals[0].id) },
                                            onToggleShoppingList = { d, fav -> onToggleShoppingList(d, fav) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        if (rowDeals.size > 1) {
                                            DealCard(
                                                deal = rowDeals[1],
                                                onClick = { onDealClick(rowDeals[1].id) },
                                                onToggleShoppingList = { d, fav -> onToggleShoppingList(d, fav) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                                
                                val previousCount = cumulativeItemCount
                                cumulativeItemCount += rowDeals.size
                                justInsertedAd = false
                                
                                if (cumulativeItemCount / 20 > previousCount / 20) {
                                    val insertionIndex = cumulativeItemCount / 20
                                    val adSize = if (insertionIndex % 2 == 0) com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE else com.google.android.gms.ads.AdSize.LARGE_BANNER
                                    val adHeight = if (adSize == com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE) 250.dp else 100.dp
    
                                    item(key = "ad_banner_${category}_${rowDeals.first().id}") {
                                        com.example.omiri.ui.components.AdCard(
                                            modifier = Modifier
                                                .padding(horizontal = Spacing.lg)
                                                .height(adHeight),
                                            adSize = adSize
                                        )
                                    }
                                    justInsertedAd = true
                                }
                            }
                        }
                        
                        if (allDeals.isEmpty()) {
                            if (isLoading) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = com.example.omiri.ui.theme.AppColors.BrandOrange, strokeWidth = 4.dp)
                                    }
                                }
                            } else {
                                 item {
                                    val error by viewModel.error.collectAsState()
                                    val networkErrorType by viewModel.networkErrorType.collectAsState()
                                    com.example.omiri.ui.components.OmiriSmartEmptyState(
                                        networkErrorType = networkErrorType,
                                        error = error,
                                        onRetry = { viewModel.loadProducts() },
                                        defaultIcon = androidx.compose.material.icons.Icons.Outlined.LocalOffer,
                                        defaultTitle = "No deals found",
                                        defaultMessage = "Try adjusting your filters or search query",
                                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl)
                                    )
                                 }
                            }
                        }
                        
                        if (isPaging) {
                            item(key = "loading_indicator_bottom") {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = com.example.omiri.ui.theme.AppColors.BrandOrange, strokeWidth = 2.dp)
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(Spacing.xxxl)) }
                    }
                }

                // Collapsible Search and Filters Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            filterBarHeightPx = coordinates.size.height.toFloat()
                        }
                        .graphicsLayer { translationY = filterBarOffsetPx }
                        .background(Color.White)
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                        Spacer(Modifier.height(Spacing.sm))
                        val searchQuery by viewModel.searchQuery.collectAsState()
                        
                        OmiriSearchBar(
                            value = searchQuery,
                            onQueryChange = { query ->
                                if (query.length > 2) {
                                    viewModel.searchProducts(query)
                                } else if (query.isEmpty()) {
                                     viewModel.loadProducts()
                                     viewModel.searchProducts("")
                                } else {
                                     viewModel.searchProducts(query)
                                }
                            }
                        )
                        Spacer(Modifier.height(Spacing.md))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                            containerColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                                            contentColor = com.example.omiri.ui.theme.AppColors.Surface
                                        ) {
                                            Text(text = activeFilterCount.toString(), style = MaterialTheme.typography.labelSmall)
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
                                        tint = if (hasActiveFilters) com.example.omiri.ui.theme.AppColors.BrandOrange else com.example.omiri.ui.theme.AppColors.BrandInk
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
                                selectedBackgroundColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                                selectedTextColor = com.example.omiri.ui.theme.AppColors.Surface,
                                unselectedBackgroundColor = com.example.omiri.ui.theme.AppColors.PastelGrey,
                                unselectedTextColor = com.example.omiri.ui.theme.AppColors.BrandInk
                            )
                        }

                        
                        Spacer(Modifier.height(Spacing.lg))
                    }
                    // Bottom border for the filter bar
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                }
            }
        }
    }
    
    // Filter Modal
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
                hasDiscount = filters.hasDiscount,
                categories = filters.selectedCategories
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
                    selectedContainerColor = com.example.omiri.ui.theme.AppColors.BrandOrange,
                    selectedLabelColor = com.example.omiri.ui.theme.AppColors.Surface,
                    containerColor = Color.White,
                    labelColor = Color(0xFF374151)
                ),
                border = FilterChipDefaults.filterChipBorder(
                     enabled = true,
                     selected = isSelected,
                     borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                     borderWidth = 1.dp
                )
            )
        }
    }
}
