package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.components.DealCard
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListMatchesScreen(
    viewModel: ProductViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDealClick: (String) -> Unit = {}
) {
    val matches by viewModel.shoppingListMatches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkShoppingListMatches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Shopping List Matches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Deals for your list items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.omiri.ui.theme.AppColors.Bg
                )
            )
        },
        containerColor = com.example.omiri.ui.theme.AppColors.Bg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     CircularProgressIndicator(color = Color(0xFFEA580B))
                 }
            } else if (matches.isEmpty()) {
                 // Smart Empty State
                 val error by viewModel.error.collectAsState()
                 val networkErrorType by viewModel.networkErrorType.collectAsState()
                 
                 com.example.omiri.ui.components.OmiriSmartEmptyState(
                     networkErrorType = networkErrorType,
                     error = error,
                     onRetry = { viewModel.checkShoppingListMatches() },
                     defaultIcon = androidx.compose.material.icons.Icons.Outlined.SentimentDissatisfied, // Or LocalOffer? SentimentDissatisfied was used before.
                     defaultTitle = "No matches found",
                     defaultMessage = "None of your shopping list items are currently on sale.",
                     modifier = Modifier.padding(Spacing.xxl)
                 )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xl)
                ) {
                    matches.forEach { (category, deals) ->
                        // Header
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF3F4F6)
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.lg, 
                                        vertical = Spacing.sm
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                            }
                        }

                        // Deals Grid (Rows of 2)
                        val chunkedDeals = deals.chunked(2)
                        items(chunkedDeals) { rowDeals ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                DealCard(
                                    deal = rowDeals[0],
                                    onClick = { onDealClick(rowDeals[0].id) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                if (rowDeals.size > 1) {
                                    DealCard(
                                        deal = rowDeals[1],
                                        onClick = { onDealClick(rowDeals[1].id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
