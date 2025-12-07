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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val potentialSavings by viewModel.shoppingListSavings.collectAsState(initial = 0.0)
    
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
                onNotificationClick = onNotificationsClick
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB)) // Overall background
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Header with Name and Savings Summary
                com.example.omiri.ui.components.HomeHeader(
                    potentialSavings = "€%.2f".format(if (potentialSavings > 0) potentialSavings else 18.40) // Mock default if 0
                )

                // 2. Smart Plan Card
                Spacer(Modifier.height(Spacing.md))
                com.example.omiri.ui.components.SmartPlanCard(
                    onStartPlan = {},
                    onAdjustPlan = {}
                )

                // 3. Smart Alerts
                Spacer(Modifier.height(Spacing.lg))
                com.example.omiri.ui.components.SmartAlertsCard()

                // 4. Shopping Lists
                Spacer(Modifier.height(Spacing.xl))
                com.example.omiri.ui.components.ShoppingListsSection(
                    onViewAll = onNavigateAllDeals
                )

                // 5. Featured Deals
                Spacer(Modifier.height(Spacing.xl))
                com.example.omiri.ui.components.FeaturedDealsRow(
                    deals = featuredDeals.take(5), // Dynamically use real deals
                    onViewAll = onNavigateAllDeals,
                    onDealClick = onDealClick
                )

                Spacer(Modifier.height(Spacing.md))
            }
        }
    }
}