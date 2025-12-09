package com.example.omiri.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.AsyncImage
import com.example.omiri.R
import com.example.omiri.data.api.models.ProductResponse
import com.example.omiri.data.api.models.StoreListResponse
import com.example.omiri.ui.components.DealsCarousel
import com.example.omiri.ui.components.OmiriHeader
import com.example.omiri.ui.components.SectionHeader

import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ProductViewModel


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateAllDeals: () -> Unit = {},
    onDealClick: (String) -> Unit = {},
    fixedHeader: Boolean = true,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onToggleShoppingList: (com.example.omiri.data.models.Deal, Boolean) -> Unit = { _, _ -> },
    onNavigateToShoppingListTab: () -> Unit = {},
    onNavigateToList: (String) -> Unit = {},
    viewModel: ProductViewModel,
    shoppingListViewModel: com.example.omiri.viewmodels.ShoppingListViewModel
) {
    // Collect state from ViewModel
    val featuredDeals by viewModel.featuredDeals.collectAsState()
    val shoppingListDeals by viewModel.shoppingListDeals.collectAsState()
    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val potentialSavings by viewModel.shoppingListSavings.collectAsState(initial = 0.0)
    val smartPlan by viewModel.smartPlan.collectAsState()
    val smartAlerts by viewModel.smartAlerts.collectAsState()
    
    // AdMob Interstitial
    val context = androidx.compose.ui.platform.LocalContext.current
    val adManager = remember { com.example.omiri.ui.components.InterstitialAdManager(context) }
    
    LaunchedEffect(Unit) {
        viewModel.initialLoad()
        viewModel.loadCategoriesIfNeeded()
        adManager.loadAd()
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
        Column(modifier = Modifier.fillMaxSize()) {
            // 0. Omiri Header (Fixed at top)
            com.example.omiri.ui.components.OmiriHeader(
                notificationCount = 2,
                onNotificationClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.example.omiri.ui.theme.AppColors.Bg) // Overall background
                    .verticalScroll(rememberScrollState())
            ) {


                // Removed global error block as per user request
                // We will handle errors inside sections (e.g. FeaturedDealsRow)
                
                Spacer(modifier = Modifier.height(Spacing.lg))

                // 2 & 3. Smart Plan / Alerts Carousel
                
                // Determine pages. 
                val pages = mutableListOf<@Composable () -> Unit>()
                
                if (smartPlan != null && smartPlan!!.steps.isNotEmpty()) {
                    pages.add { com.example.omiri.ui.components.SmartPlanCard(plan = smartPlan) }
                }
                
                // Smart Alerts removed as per request to merge/remove.
                // if (smartAlerts.isNotEmpty()) {
                //      pages.add { com.example.omiri.ui.components.SmartAlertsCard(alerts = smartAlerts) }
                // }
                
                if (pages.isNotEmpty()) {
                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pages.size })
                    
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        pageSpacing = Spacing.md
                    ) { page ->
                        pages.getOrNull(page)?.invoke()
                    }
                    
                    // Indicators (only if > 1 page)
                    if (pagerState.pageCount > 1) {
                        Spacer(Modifier.height(Spacing.sm))
                        Row(
                            Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(pagerState.pageCount) { iteration ->
                                 val color = if (pagerState.currentPage == iteration) Color(0xFFFE8357) else Color(0xFFD1D5DB)
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .size(6.dp)
                                        .background(color, androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                    }
                    
                    // Spacing after smart section before next section
                    Spacer(Modifier.height(Spacing.lg))
                }
                
                // 4. Shopping Lists
                com.example.omiri.ui.components.ShoppingListsSection(
                    shoppingLists = shoppingLists,
                    onViewAll = onNavigateToShoppingListTab,
                    onListClick = onNavigateToList
                )

                // 5. Featured Deals
                Spacer(Modifier.height(Spacing.xl))
                // Pass error context
                val networkErrorType by viewModel.networkErrorType.collectAsState()
                
                com.example.omiri.ui.components.FeaturedDealsRow(
                    deals = featuredDeals.take(5), 
                    isLoading = isLoading,
                    error = error,
                    networkErrorType = networkErrorType,
                    onRetry = { viewModel.loadProducts() }, // or specific loadFeaturedDeals
                    onViewAll = onNavigateAllDeals,
                    onDealClick = onDealClick
                )

                Spacer(Modifier.height(Spacing.md))
            }
        }
    }
}