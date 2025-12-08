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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.components.NotificationCard
import com.example.omiri.ui.models.NotificationUiModel
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.NotificationViewModel
import com.example.omiri.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    // Grouping Logic
    val groupedNotifications = remember(notifications) {
        val today = notifications.filter { 
            it.time.contains("m ago") || it.time.contains("h ago") || it.time.contains("Just now")
        }
        val yesterday = notifications.filter { it.time.contains("1d") }
        val older = notifications.filter { !today.contains(it) && !yesterday.contains(it) }
        
        mapOf(
            "TODAY" to today,
            "YESTERDAY" to yesterday,
            "OLDER" to older
        ).filter { it.value.isNotEmpty() }
    }

    val filters = listOf("All", "Deals", "Price Drops", "Lists")
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = Color(0xFFF9FAFB), // Very light gray background
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                ScreenHeader(
                    title = "Notifications",
                    onBackClick = onBackClick,
                    action = {
                        TextButton(onClick = { /* Mark all read */ }) {
                            Text(
                                "Mark all read",
                                color = Color(0xFFF97316),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
                
                // Filters
                
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
