package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.data.dummy.DummyDeals
import com.example.omiri.ui.components.DealsCarousel
import com.example.omiri.ui.components.SectionHeader
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    dealId: String? = null,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    viewModel: com.example.omiri.viewmodels.ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var deal by remember { mutableStateOf<com.example.omiri.data.models.Deal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(dealId) {
        if (dealId != null) {
            viewModel.getProductById(dealId) { loadedDeal ->
                deal = loadedDeal
                isFavorite = loadedDeal?.isFavorite == true
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFEA580B))
        }
        return
    }

    val currentDeal = deal

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = Spacing.sm),
                title = {
                    Text(
                        if (currentDeal != null) currentDeal.store else "Product Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )


        }
    ) { padding ->
        if (currentDeal == null) {
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Product not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(currentDeal.heroColor ?: Color(0xFFF3F4F6))
                ) {
                     // Using Image if available (Placeholder logic for now as AsyncImage usage is not confirmed in view_file imports)
                     // If imageUrl logic exists, we could use it.
                     // Assuming basic Box background for now.
                     
                    // Time left badge
                    if (currentDeal.timeLeftLabel != null) {
                        Surface(
                            modifier = Modifier
                                .padding(Spacing.md)
                                .align(Alignment.TopStart),
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFFEF4444)
                        ) {
                            Text(
                                text = currentDeal.timeLeftLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Discount badge
                    if (currentDeal.discountLabel != null) {
                        Surface(
                            modifier = Modifier
                                .padding(start = Spacing.md, top = if (currentDeal.timeLeftLabel != null) 52.dp else Spacing.md)
                                .align(Alignment.TopStart),
                            shape = MaterialTheme.shapes.small,
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = currentDeal.discountLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier
                            .padding(Spacing.md)
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF6B7280)
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(Spacing.lg)
                ) {

                    // Product Title
                    Text(
                        text = currentDeal.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    // Description (Simulated)
                    Text(
                        text = "Great deal available at ${currentDeal.store}. Check it out properly in store or verify availability online.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    // Price Section
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = currentDeal.price,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA580B)
                        )
                        if (currentDeal.originalPrice != null) {
                            Text(
                                text = currentDeal.originalPrice,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF9CA3AF),
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        // Save badge if calculating savings is possible
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // Status badges
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentDeal.timeLeftLabel != null) {
                            StatusBadge(
                                icon = Icons.Outlined.Schedule,
                                text = currentDeal.timeLeftLabel,
                                iconColor = Color(0xFFEF4444)
                            )
                        }
                        StatusBadge(
                            icon = Icons.Outlined.Store,
                            text = "Available at ${currentDeal.store}",
                            iconColor = Color(0xFF3B82F6)
                        )
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEA580B)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlaylistAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Add to List",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEA580B)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "View Flyer",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // Deal Information
                    Text(
                        text = "Deal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(Spacing.md))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFC)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md)
                        ) {
                            DealInfoRow("Category", currentDeal.category)
                            DealInfoRow("Retailer", currentDeal.store)
                            
                            Spacer(Modifier.height(Spacing.sm))

                            Text(
                                text = "Offers valid while stocks last. See retailer for details.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // Similar Deals (Static for now due to complexity of fetching similar)
                    SectionHeader(
                        title = "Similar Deals",
                        actionText = "View All",
                        onActionClick = { }
                    )

                    Spacer(Modifier.height(Spacing.md))
                }

                DealsCarousel(
                    deals = DummyDeals.featured.take(3), // Keeping dummy for carousel for now
                    modifier = Modifier.offset(x = (-Spacing.lg))
                )
            }
        }
    }
}


@Composable
private fun StatusBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151)
        )
    }
}

@Composable
private fun ProductDetailItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF374151)
        )
    }
}

@Composable
private fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun DealInfoRow(
    label: String,
    value: String,
    isExpiring: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isExpiring) Color(0xFFEF4444) else Color(0xFF111827)
        )
    }
}
