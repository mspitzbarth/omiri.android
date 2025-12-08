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
        Box(modifier = Modifier.fillMaxSize().background(AppColors.Bg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFEA580B))
        }
        return
    }

    Scaffold(
        containerColor = AppColors.Bg,
        topBar = {
             // Custom Header matching Settings
             ScreenHeader(
                 title = if (currentDeal != null) currentDeal.store else "Product Details",
                 onBackClick = onBackClick,
                 action = {
                     IconButton(onClick = { shareDeal() }) {
                         Icon(
                             imageVector = Icons.Outlined.Share,
                             contentDescription = "Share",
                             tint = Color(0xFF111827)
                         )
                     }
                 }
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
                        .height(280.dp) // Taller image area
                        .background(currentDeal.heroColor ?: Color(0xFFF3F4F6))
                ) {
                     // Placeholder for Image
                     // If imageUrl exists, load it.
                     
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
                    
                    // Brand Badge (New)
                    if (!currentDeal.brand.isNullOrBlank()) {
                         Surface(
                            modifier = Modifier
                                .padding(end = Spacing.md, bottom = Spacing.md)
                                .align(Alignment.BottomEnd),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = currentDeal.brand,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentDeal.id) },
                        modifier = Modifier
                            .padding(Spacing.md)
                            .align(Alignment.TopEnd)
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                            .padding(4.dp) // Inner padding
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

                    // Product Title & Brand
                    Text(
                        text = currentDeal.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    
                    if (!currentDeal.brand.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentDeal.brand,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))
                    
                    // Price Row
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = currentDeal.price,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA580B)
                        )
                        if (currentDeal.originalPrice != null) {
                            Text(
                                text = currentDeal.originalPrice,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF9CA3AF),
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(Spacing.lg))
                    
                    // Description Block
                    if (!currentDeal.description.isNullOrBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentDeal.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF4B5563),
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(Spacing.lg))
                    }

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.md)
                        ) {
                            DealInfoRow("Retailer", currentDeal.store, icon = Icons.Outlined.Store)
                            if (!currentDeal.country.isNullOrBlank()) {
                                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                                DealInfoRow("Location", "${currentDeal.country}${if (currentDeal.zipcode != null) ", ${currentDeal.zipcode}" else ""}", icon = Icons.Outlined.Place)
                            }
                            if (!currentDeal.category.isNullOrBlank()) {
                                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                                DealInfoRow("Category", currentDeal.category, icon = Icons.Outlined.Category)
                            }
                            
                            // Dates
                            if (currentDeal.availableFrom != null || currentDeal.availableUntil != null) {
                                HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
                                val from = formatIsoDate(currentDeal.availableFrom)
                                val to = formatIsoDate(currentDeal.availableUntil)
                                val dateStr = if (from != null && to != null) "$from - $to" else (to ?: from ?: "")
                                DealInfoRow("Valid", dateStr, icon = Icons.Outlined.DateRange, isHighlight = true)
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Button(
                            onClick = { viewModel.toggleShoppingList(currentDeal) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isOnList) Color(0xFF10B981) else Color(0xFFEA580B)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(if (isOnList) Icons.Outlined.Check else Icons.Outlined.PlaylistAdd, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isOnList) "Added" else "Add to List", fontWeight = FontWeight.Bold)
                        }
                        
                        if (currentDeal.pdfSourceUrl != null) {
                            OutlinedButton(
                                onClick = { onViewFlyer(currentDeal.pdfSourceUrl) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEA580B)),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Outlined.Description, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Flyer", fontWeight = FontWeight.Bold)
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

