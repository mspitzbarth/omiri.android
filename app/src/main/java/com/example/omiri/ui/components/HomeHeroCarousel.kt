package com.example.omiri.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeHeroCarousel(
    deals: List<Deal>,
    onDealClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (deals.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { deals.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            pageSpacing = Spacing.md,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            HeroDealCard(
                deal = deals[page],
                onClick = { onDealClick(deals[page].id) }
            )
        }
        
        // Indicators
        Spacer(Modifier.height(Spacing.sm))
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(6.dp)
                )
            }
        }
    }
}

@Composable
fun HeroDealCard(
    deal: Deal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image or Color
            if (!deal.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = deal.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(deal.heroColor ?: MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Spacing.lg)
            ) {
                // Badge
                if (deal.discountPercentage > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "-${deal.discountPercentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(Spacing.xs))
                }

                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White, // Always white because of gradient or solid bg assumption for now
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(Spacing.xs))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = deal.price,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = deal.store,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(Modifier.height(Spacing.md))
                
                // Shop Now Button
                Surface(
                    color = Color(0xFFEA580C), // Orange
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Shop Now",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
