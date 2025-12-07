package com.example.omiri.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(bottom = Spacing.lg)
        ) {
            item {
                com.example.omiri.ui.components.NotificationCard(
                    title = "Weekly Groceries",
                    subtitle = "8 to buy / 3 done",
                    time = "2 hours ago",
                    icon = Icons.Outlined.ShoppingCart,
                    color = Color(0xFF3B82F6), // Blue
                    chips = listOf("Lidl", "3 items in deals")
                )
            }
            
            item {
                com.example.omiri.ui.components.NotificationCard(
                    title = "Costco Run",
                    subtitle = "5 to buy / 0 done",
                    time = "1 day ago",
                    icon = Icons.Outlined.Storefront,
                    color = Color(0xFFA855F7), // Purple
                    chips = listOf("Costco", "2 items in deals")
                )
            }
            
            item {
                com.example.omiri.ui.components.NotificationCard(
                    title = "Meal Prep",
                    subtitle = "12 to buy / 2 done",
                    time = "3 hours ago",
                    icon = Icons.Outlined.Restaurant,
                    color = Color(0xFF10B981), // Green
                    chips = listOf("Aldi", "1 item in deals")
                )
            }
            
            item {
                com.example.omiri.ui.components.NotificationCard(
                    title = "Party Supplies",
                    subtitle = "7 to buy / 1 done",
                    time = "5 hours ago",
                    icon = Icons.Outlined.Cake,
                    color = Color(0xFFEC4899), // Pink
                    chips = listOf("Kaufland", "2 items in deals")
                )
            }
        }
    }
}
