package com.example.omiri.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.data.dummy.DummyDeals
import com.example.omiri.ui.components.DealsCarousel
import com.example.omiri.ui.components.ScreenHeader
import com.example.omiri.ui.components.SectionHeader
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Locale
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    dealId: String? = null,
    onBackClick: () -> Unit = {},
    onShareClick: (() -> Unit)? = null, // Optional if handled internally
    onAddToList: (com.example.omiri.data.models.Deal) -> Unit = {},
    onViewFlyer: (String) -> Unit = {},
    viewModel: com.example.omiri.viewmodels.ProductViewModel
) {
    var deal by remember { mutableStateOf<com.example.omiri.data.models.Deal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val favorites by viewModel.favoriteDealIds.collectAsState()
    val shoppingListDeals by viewModel.shoppingListDeals.collectAsState()
    
    val context = LocalContext.current

    LaunchedEffect(dealId) {
        if (dealId != null) {
            viewModel.getProductById(dealId) { loadedDeal ->
                deal = loadedDeal
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    val currentDeal = deal
    val isFavorite = currentDeal != null && favorites.contains(currentDeal.id)
    val isOnList = currentDeal != null && shoppingListDeals.any { it.id == currentDeal.id }
    
    // Internal Share Handler
    fun shareDeal() {
        val d = currentDeal ?: return
        val deepLink = "omiri://deal/${d.id}"
        val shareText = "Check out this deal for ${d.title} at ${d.store}: $deepLink"
        val htmlText = "Check out this deal for <b>${d.title}</b> at <b>${d.store}</b>: <a href=\"$deepLink\">View Deal</a>"
        
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Deal")
        context.startActivity(shareIntent)
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFEA580B))
        }
    } else if (deal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found", color = MaterialTheme.colorScheme.onBackground)
        }
    } else {
        val currentDeal = deal!! // Safe unwrapping as we checked for null
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Bg)
        ) {
            // Header (Settings Style)
            ScreenHeader(
                title = currentDeal.store,
                onBackClick = onBackClick,
                action = {
                    IconButton(onClick = { shareDeal() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = AppColors.BrandInk
                        )
                    }
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image or Emoji
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentDeal.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentDeal.imageUrl,
                            contentDescription = currentDeal.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                         // Emoji Fallback
                         val emoji = com.example.omiri.util.EmojiHelper.getProductEmoji(currentDeal.title, currentDeal.category)
                         if (emoji.isNotEmpty()) {
                             Text(text = emoji, fontSize = 96.sp)
                         } else {
                             Icon(
                                 imageVector = Icons.Outlined.ShoppingBag,
                                 contentDescription = null,
                                 tint = Color.LightGray,
                                 modifier = Modifier.size(64.dp)
                             )
                         }
                    }
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title & Price
                    Text(
                        text = currentDeal.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.BrandInk
                    )
                    
                    if (!currentDeal.brand.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDeal.brand,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.MutedText
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentDeal.price,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandOrange
                            )
                        )
                        if (!currentDeal.originalPrice.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = currentDeal.originalPrice,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = AppColors.SubtleText
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Main Actions (Heart & Flyer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Heart Button (Primary Action) - Red Styled
                        Button(
                            onClick = { onAddToList(currentDeal) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOnList) Color(0xFFDC2626) else Color.White,
                                contentColor = if (isOnList) Color.White else Color(0xFFDC2626)
                            ),
                            border = if (!isOnList) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)) else null,
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                             Icon(
                                imageVector = if (isOnList) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isOnList) "Remove from list" else "Add to list",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Flyer Button (Secondary) - Solid Orange
                        if (!currentDeal.pdfSourceUrl.isNullOrBlank()) {
                            Button(
                                onClick = { onViewFlyer(currentDeal.pdfSourceUrl) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.BrandOrange,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description, 
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Flyer", 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Description
                    if (!currentDeal.description.isNullOrBlank()) {
                        SectionHeader(title = "Description")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentDeal.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.MutedText,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md)
                        ) {
                            DealInfoRow("Retailer", currentDeal.store, icon = Icons.Outlined.Store)
                            if (!currentDeal.country.isNullOrBlank()) {
                                HorizontalDivider(color = AppColors.SurfaceAlt, modifier = Modifier.padding(vertical = 12.dp))
                                DealInfoRow("Location", "${currentDeal.country}${if (currentDeal.zipcode != null) ", ${currentDeal.zipcode}" else ""}", icon = Icons.Outlined.Place)
                            }
                            if (!currentDeal.category.isNullOrBlank()) {
                                HorizontalDivider(color = AppColors.SurfaceAlt, modifier = Modifier.padding(vertical = 12.dp))
                                DealInfoRow("Category", currentDeal.category, icon = Icons.Outlined.Category)
                            }
                            
                            // Dates
                            if (currentDeal.availableFrom != null || currentDeal.availableUntil != null) {
                                HorizontalDivider(color = AppColors.SurfaceAlt, modifier = Modifier.padding(vertical = 12.dp))
                                val from = formatIsoDate(currentDeal.availableFrom)
                                val to = formatIsoDate(currentDeal.availableUntil)
                                val dateStr = if (from != null && to != null) "$from - $to" else (to ?: from ?: "")
                                DealInfoRow("Valid", dateStr, icon = Icons.Outlined.DateRange, isHighlight = true)
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun DealInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlight) Color(0xFFEA580B) else Color(0xFF111827)
        )
    }
}

private fun formatIsoDate(isoDate: String?): String? {
    if (isoDate == null) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        date?.let { formatter.format(it) }
    } catch (e: Exception) {
        isoDate
    }
}

