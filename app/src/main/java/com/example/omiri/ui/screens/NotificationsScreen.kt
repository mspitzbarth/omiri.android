package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.components.NotificationCard
import com.example.omiri.ui.models.NotificationUiModel
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    // Mock Data
    val notifications = remember {
        listOf(
            // Today
            NotificationUiModel.FlashSale(
                id = "1",
                title = "Flash Sale Alert!",
                time = "5m ago",
                isRead = false,
                icon = Icons.Outlined.LocalFireDepartment,
                iconColor = Color(0xFFEF4444), // Red
                description = "Hamburger patties at Target now 30% off! Limited time only.",
                timeLeft = "2 hours left",
                savings = "Save $3.60"
            ),
            NotificationUiModel.PriceDrop(
                id = "2",
                title = "Price Drop",
                time = "1h ago",
                isRead = false,
                icon = Icons.Outlined.ArrowDownward,
                iconColor = Color(0xFF10B981), // Green
                description = "Wireless Headphones dropped from $89 to $64 at Best Buy",
                currentPrice = "$64",
                originalPrice = "$89",
                discountPercentage = "28% off"
            ),
            NotificationUiModel.ListUpdate(
                id = "3",
                title = "Shopping List Update",
                time = "2h ago",
                isRead = false,
                icon = Icons.AutoMirrored.Outlined.List,
                iconColor = Color(0xFF3B82F6), // Blue
                description = "3 items from \"BBQ Party List\" are now on sale nearby",
                locationTag = "Walmart • 2.3 mi"
            ),
            NotificationUiModel.Reward(
                id = "4",
                title = "Reward Earned!",
                time = "3h ago",
                isRead = true,
                icon = Icons.Outlined.CardGiftcard,
                iconColor = Color(0xFFA855F7), // Purple
                description = "You've earned 500 points at Target. Redeem for $5 off!",
                pointsTag = "500 points"
            ),
            // Yesterday
            NotificationUiModel.General(
                id = "5",
                title = "New Coupon Available",
                time = "1d ago",
                isRead = true,
                icon = Icons.Outlined.LocalOffer,
                iconColor = Color(0xFFF97316), // Orange
                description = "$10 off your next purchase of $50+ at Whole Foods",
                tag = "Expires in 7 days"
            ),
            NotificationUiModel.General(
                id = "6",
                title = "Deal Ending Soon",
                time = "1d ago",
                isRead = true,
                icon = Icons.Outlined.AccessTime,
                iconColor = Color(0xFFEAB308), // Yellow
                description = "Your saved deal on Hot Dogs expires tomorrow at Target",
                tag = "1 day left"
            ),
            NotificationUiModel.PriceDrop(
                id = "7",
                title = "Wishlist Item on Sale",
                time = "1d ago",
                isRead = true,
                icon = Icons.Outlined.Favorite,
                iconColor = Color(0xFFEC4899), // Pink
                description = "Summer Beach Towel Set is now 40% off at Amazon",
                currentPrice = "$23.99",
                originalPrice = "$39.99",
                discountPercentage = "40% off"
            ),
            // This Week
            NotificationUiModel.General(
                id = "8",
                title = "Weekly Savings Report",
                time = "3d ago",
                isRead = true,
                icon = Icons.Outlined.Notifications,
                iconColor = Color(0xFF6366F1), // Indigo
                description = "You saved $47.30 this week across 8 purchases!",
                actionLabel = "View Report"
            )
        )
    }

    // Grouping
    val groupedNotifications = remember(notifications) {
        mapOf(
            "TODAY" to notifications.subList(0, 4),
            "YESTERDAY" to notifications.subList(4, 7),
            "THIS WEEK" to notifications.subList(7, 8)
        )
    }

    val filters = listOf("All", "Deals", "Price Drops", "Lists")
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Color(0xFFF9FAFB), // Very light gray background
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Notifications",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "12 unread",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { /* Mark all read */ }) {
                            Text(
                                "Mark all read",
                                color = Color(0xFFF97316),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = Color(0xFFF97316)
                    )
                )
                
                // Filters
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg)
                        .padding(bottom = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = filter == selectedFilter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF97316),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF3F4F6),
                                labelColor = Color(0xFF374151)
                            ),
                            border = null
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = Spacing.lg)
        ) {
            groupedNotifications.forEach { (header, items) ->
                item {
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                            .padding(top = Spacing.sm)
                    )
                }
                
                items(items) { notification ->
                    NotificationCard(
                        notification = notification,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 4.dp),
                        onCardClick = { /* Handle click */ },
                        onActionClick = { /* Handle action */ }
                    )
                }
            }
        }
    }
}
