package com.example.omiri.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.omiri.data.models.Deal
import com.example.omiri.ui.components.DealCard
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun ProductDetailsScreen(
    dealId: String? = null,
    onBackClick: () -> Unit = {},
    onShareClick: (() -> Unit)? = null,
    onAddToList: (Deal, Boolean) -> Unit = { _, _ -> },
    onViewFlyer: (String) -> Unit = {},
    onDealClick: (String) -> Unit = {},
    viewModel: com.example.omiri.viewmodels.ProductViewModel
) {
    var deal by remember { mutableStateOf<Deal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showImageModal by remember { mutableStateOf(false) }
    
    val favorites by viewModel.favoriteDealIds.collectAsState()
    val shoppingListDeals by viewModel.shoppingListDeals.collectAsState()
    val allDeals by viewModel.allDeals.collectAsState()
    
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
    
    // Similar Deals Logic
    val similarDeals = remember(currentDeal, allDeals) {
        if (currentDeal != null) {
            allDeals.filter { 
                it.id != currentDeal.id && 
                (it.category == currentDeal.category || it.store == currentDeal.store) 
            }.take(4)
        } else emptyList()
    }
    
    // Internal Share Handler
    fun shareDeal() {
        val d = currentDeal ?: return
        val deepLink = "omiri://deal/${d.id}"
        val shareText = "Check out this deal for ${d.title} at ${d.store}: $deepLink"
        
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Deal")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
             com.example.omiri.ui.components.ScreenHeader(
                title = "Product Details",
                onBackClick = onBackClick,
                action = {
                   IconButton(onClick = { shareDeal() }) {
                       Icon(
                           imageVector = Icons.Outlined.Share,
                           contentDescription = "Share",
                           tint = AppColors.BrandInk
                       )
                   }
                }
            )
        },
        containerColor = AppColors.Bg
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.BrandOrange)
            }
        } else if (currentDeal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Product not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Image Header Area (Overlaid)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(com.example.omiri.util.EmojiHelper.getCategoryColor(currentDeal.category))
                        .clickable { if (!currentDeal.imageUrl.isNullOrBlank()) showImageModal = true }
                ) {
                    if (!currentDeal.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentDeal.imageUrl,
                            contentDescription = currentDeal.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             val emoji = com.example.omiri.util.EmojiHelper.getProductEmoji(currentDeal.title, currentDeal.category)
                             Text(text = emoji, fontSize = 96.sp)
                         }
                    }
                    
                    // Actions Overlay Removed
                    
                    // Share Button Floating (Optional: add back if needed, but Header handles navigation. 
                    // Let's keep a floating Share button at top right since it wasn't in the Header action I just added? 
                    // No, cleaner UI -> usually Share is in top bar too.
                    // But I strictly followed instruction "give me back the header".
                    // I will add Share to the header if there is space, but ScreenHeader action handles one composable.
                    // I'll stick to just Favorite in Header for now or add a Row there.
                    // Let's add Share to the Header action too.
                    
                    // Actually, I can't effectively edit the previous chunk to add Share easily without complex nesting. 
                    // I'll leave Share out for a moment or rely on user asking for it, OR
                    // I'll put a floating share FAB? No.
                    // The standard Android pattern is Share in Top Bar.
                    // I'll edit the first chunk to include Share next.
                    
                    // For now, removing the overlay buttons.

                    
                    // Time Left Badge
                    val timeLeft = DateUtils.getDaysRemaining(currentDeal.availableUntil)
                    if (timeLeft != null && timeLeft > 0) {
                         Surface(
                            color = AppColors.BrandOrange,
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 56.dp)
                         ) {
                             Text(
                                 text = if(timeLeft == 1L) "1 day left" else "$timeLeft days left",
                                 color = AppColors.Surface,
                                 style = MaterialTheme.typography.labelMedium,
                                 fontWeight = FontWeight.Bold,
                                 modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                             )
                         }
                    }
                }
                
                // 2. Body Content (Cards) - Shifted up to overlap image
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                ) {
                    // Main Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, AppColors.Border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Store Info Header removed as requested ("already have more details")
                            
                            Text(
                                text = currentDeal.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandInk
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currentDeal.price,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.BrandOrange,
                                    modifier = Modifier.offset(y = 4.dp) // Optical alignment for large text
                                )
                                
                                if (currentDeal.hasDiscount && !currentDeal.originalPrice.isNullOrBlank()) {
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                         Text(
                                            text = currentDeal.originalPrice,
                                            style = MaterialTheme.typography.titleMedium,
                                            textDecoration = TextDecoration.LineThrough,
                                            color = AppColors.MutedText
                                        )
                                    }

                                    Spacer(Modifier.width(12.dp))
                                    
                                    val saved = calculateSavings(currentDeal.price, currentDeal.originalPrice)
                                    if (saved != null) {
                                        Surface(
                                            color = AppColors.Red50,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(bottom = 6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Red200)
                                        ) {
                                            Text(
                                                text = "Save $saved",
                                                color = AppColors.Success,
                                                style = MaterialTheme.typography.labelLarge, // Larger font
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Action Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { onAddToList(currentDeal, !isOnList) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(isOnList) AppColors.Success else AppColors.BrandOrange
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(
                                        imageVector = if(isOnList) Icons.Outlined.Check else Icons.Outlined.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if(isOnList) "Added to Shopping List" else "Add to Shopping List",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                // "Calculator"/PDF Button
                                if (!currentDeal.pdfSourceUrl.isNullOrBlank()) {
                                    Spacer(Modifier.width(12.dp))
                                    Surface(
                                        onClick = { onViewFlyer(currentDeal.pdfSourceUrl) },
                                        modifier = Modifier.size(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = AppColors.Bg, 
                                        border = BorderStroke(1.dp, Color.Transparent)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.PictureAsPdf, 
                                                contentDescription = "View Flyer",
                                                tint = AppColors.SubTextGrey,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Description
                            if (!currentDeal.description.isNullOrBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val lines = currentDeal.description.split("\n", "•").filter { it.isNotBlank() }
                                    if (lines.size > 1) {
                                        lines.forEach { line ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Icon(
                                                    imageVector = Icons.Outlined.CheckCircle, 
                                                    contentDescription = null,
                                                    tint = AppColors.Neutral500,
                                                    modifier = Modifier.size(16.dp).offset(y = 2.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = line.trim(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.SubTextGrey
                                                )
                                            }
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text(
                                                text = currentDeal.description.trim(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AppColors.SubTextGrey,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 3. Store Info Card
                    Spacer(Modifier.height(16.dp)) // Restored gap (but layout gap is gone due to wrapper)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, AppColors.Border)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Store Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandInk
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.Info),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentDeal.store.take(1),
                                        color = AppColors.Surface,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = currentDeal.store,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.BrandInk
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    
                                    val locationParts = listOfNotNull(
                                        currentDeal.country?.ifBlank { null },
                                        currentDeal.zipcode?.ifBlank { null }
                                    )
                                    if (locationParts.isNotEmpty()) {
                                        Text(
                                            text = locationParts.joinToString(" • "),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.MutedText
                                        )
                                    }
                                    
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "View Store Details", 
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AppColors.BrandOrange,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { /* TODO */ }
                                    )
                                }
                            }
                            
                            if (currentDeal.hasDiscount) {
                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = AppColors.Bg, modifier = Modifier.padding(bottom = 12.dp))
                                AvailabilityRow("Availability", "In Stock", isGreen = true)
                                AvailabilityRow("Delivery", "Check App")
                                AvailabilityRow("Pickup", "Available today")
                            }
                        }
                    }
                    
                    // 4. Details Card
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, AppColors.Border)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = "Deal Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandInk
                            )
                            Spacer(Modifier.height(16.dp))
                            
                            val originalPriceDisplay = if (currentDeal.hasDiscount) (currentDeal.originalPrice ?: "-") else currentDeal.price
                            val discountDisplay = if (currentDeal.hasDiscount && currentDeal.discountPercentage > 0) "-${currentDeal.discountPercentage}%" else "-"
                            val dealPriceDisplay = if (currentDeal.hasDiscount) currentDeal.price else "-"
                            
                            DealDetailRow("Original Price", originalPriceDisplay)
                            DealDetailRow("Discount", discountDisplay, isRed = currentDeal.hasDiscount)
                            DealDetailRow("Deal Price", dealPriceDisplay, isBold = true, isOrange = currentDeal.hasDiscount)
                            
                            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AppColors.Bg)
                            
                            val from = formatIsoDate(currentDeal.availableFrom)
                            val to = formatIsoDate(currentDeal.availableUntil)
                            val dateRange = if (from != null && to != null) "$from - $to" else (to ?: from ?: "-")
                            DealDetailRow("Valid", dateRange)
                        }
                    }
                    
                    // 5. Similar Deals
                    if (similarDeals.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Similar Deals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BrandInk,
                            modifier = Modifier.padding(horizontal = Spacing.md)
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarDeals.size) { index ->
                                val item = similarDeals[index]
                                Box(modifier = Modifier.width(160.dp)) {
                                    DealCard(
                                        deal = item,
                                        modifier = Modifier.width(160.dp),
                                        onClick = { onDealClick(item.id) },
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }

            }
        }
    }

    if (showImageModal && currentDeal?.imageUrl != null) {
        Dialog(
            onDismissRequest = { showImageModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            
            val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                if (scale > 1f) {
                    val maxOffset = (scale - 1f) * 1000f // Approximate bounds check
                    // Simple panning logic
                    val newOffset = offset + offsetChange
                    offset = Offset(
                        x = newOffset.x, //.coerceIn(-maxOffset, maxOffset), // detailed bounds need view size
                        y = newOffset.y
                    )
                } else {
                    offset = Offset.Zero
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showImageModal = false }, // Tap background to close
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentDeal.imageUrl,
                    contentDescription = currentDeal.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = state)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                             // Consume click so it doesn't close the modal when tapping the image itself,
                             // unless user wants that. Usually we don't close on image tap if likely to zoom.
                             // But let's leave default propagation (closes modal) OR block it.
                             // Better UX: Tap image usually toggles UI or does nothing. 
                             // Clicking background closes.
                        }
                )

                IconButton(
                    onClick = { showImageModal = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .padding(top = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AvailabilityRow(label: String, value: String, isGreen: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = AppColors.MutedText)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Medium,
            color = if(isGreen) AppColors.Success else AppColors.BrandInk
        )
    }
}

@Composable
fun DealDetailRow(label: String, value: String, isRed: Boolean = false, isBold: Boolean = false, isOrange: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = AppColors.SubTextGrey)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = if(isBold) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isRed -> AppColors.Danger
                isOrange -> AppColors.BrandOrange
                else -> AppColors.BrandInk
            }
        )
    }
}

private fun calculateSavings(price: String, original: String?): String? {
    if (original == null) return null
    return try {
        val p = price.replace(Regex("[^0-9.]"), "").toDouble()
        val o = original.replace(Regex("[^0-9.]"), "").toDouble()
        if (o > p) {
            val diff = o - p
            val symbol = if (price.contains("€")) "€" else "$"
            "$symbol${String.format("%.2f", diff)}"
        } else null
    } catch (e: Exception) { null }
}

private fun formatIsoDate(isoDate: String?): String? {
    if (isoDate == null) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(isoDate)
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        date?.let { formatter.format(it) }
    } catch (e: Exception) {
        isoDate
    }
}
