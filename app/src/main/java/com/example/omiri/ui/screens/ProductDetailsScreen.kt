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
import androidx.compose.material.icons.filled.Add
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
    onViewFlyer: (String, String, Int?) -> Unit = { _, _, _ -> },
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
                title = "Deal Details",
                onBackClick = onBackClick,
                action = {
                    Row {
                        IconButton(onClick = { shareDeal() }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = AppColors.BrandInk
                            )
                        }
                        IconButton(onClick = { /* Toggle Favorite - Logic needed in VM but icon state is enough for UI */ }) {
                            Icon(
                                imageVector = if(isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if(isFavorite) AppColors.BrandOrange else AppColors.BrandInk
                            )
                        }
                    }
                }
            )
        },
        containerColor = AppColors.Bg // Cards will stand out on this grey background
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.BrandOrange)
            }
        } else if (currentDeal == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Product not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Apply padding from Scaffold
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Large image area
                        .background(Color.White)
                        .clickable { if (!currentDeal.imageUrl.isNullOrBlank()) showImageModal = true }
                ) {
                    if (!currentDeal.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentDeal.imageUrl,
                            contentDescription = currentDeal.title,
                            contentScale = ContentScale.Fit, // Contain image as per design
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        )
                    } else {
                         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             val emoji = com.example.omiri.util.EmojiHelper.getProductEmoji(currentDeal.title, currentDeal.category)
                             Text(text = emoji, fontSize = 96.sp)
                         }
                    }
                    
                    // Badges (Overlaid)
                    // Save Badge (Top Left)
                    val saved = calculateSavings(currentDeal.price, currentDeal.originalPrice)
                    if (saved != null) {
                        Surface(
                            color = AppColors.Danger,
                            shape = RoundedCornerShape(50.dp), // Pill shape
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "Save $saved",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    // Time Left Badge (Top Right)
                    val timeLeft = DateUtils.getDaysRemaining(currentDeal.availableUntil)
                    if (timeLeft != null && timeLeft > 0) {
                        Surface(
                            color = AppColors.BrandOrange,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.TopEnd)
                        ) {
                             Row(
                                 verticalAlignment = Alignment.CenterVertically,
                                 modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                             ) {
                                 Icon(Icons.Outlined.AccessTime, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                 Spacer(Modifier.width(4.dp))
                                 Text(
                                     text = if(timeLeft == 1L) "1 day left" else "$timeLeft days left",
                                     color = Color.White,
                                     style = MaterialTheme.typography.labelMedium,
                                     fontWeight = FontWeight.Bold
                                 )
                             }
                        }
                    }
                }
                
                // 2. Main Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White) // Lower part is white too? Or slight gray separation. Design looks like one long scroll.
                        .padding(horizontal = 16.dp)
                ) {
                    // Store & Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Store Icon (Small rounded square)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AppColors.Red50, RoundedCornerShape(6.dp)), // Assuming Target-ish red
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Storefront, null, tint = AppColors.Danger, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${currentDeal.store}  •  ${currentDeal.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.MutedText
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Title
                    Text(
                        text = currentDeal.title,
                        style = MaterialTheme.typography.headlineSmall, // Larger title
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandInk
                    )
                    
                    // Optional Description/Subtitle
                    if (!currentDeal.description.isNullOrBlank()) {
                         Spacer(Modifier.height(4.dp))
                         Text(
                             text = currentDeal.description.take(60) + if(currentDeal.description.length > 60) "..." else "", // Show short snippet or variant
                             style = MaterialTheme.typography.bodyMedium,
                             color = AppColors.SubTextGrey
                         )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Price Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentDeal.price,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BrandOrange
                        )
                        
                        if (currentDeal.hasDiscount && !currentDeal.originalPrice.isNullOrBlank()) {
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = currentDeal.originalPrice,
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = TextDecoration.LineThrough,
                                color = AppColors.MutedText
                            )
                            Spacer(Modifier.width(12.dp))
                            if (currentDeal.discountPercentage > 0) {
                                Surface(
                                    color = AppColors.Green100,
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Text(
                                        text = "${currentDeal.discountPercentage}% OFF",
                                        color = AppColors.GreenTextDark,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onAddToList(currentDeal, !isOnList) },
                            modifier = Modifier
                                .weight(1f)
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandOrange)
                        ) {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add to List", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        // Flyer Button
                        if (!currentDeal.pdfSourceUrl.isNullOrBlank()) {
                            Surface(
                                onClick = { onViewFlyer(currentDeal.pdfSourceUrl, currentDeal.store, currentDeal.pageNumber) },
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.Bg,
                                border = BorderStroke(1.dp, AppColors.Border) // Or none
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.PictureAsPdf,
                                        contentDescription = "PDF",
                                        tint = AppColors.BrandInk
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // 3. Deal Validity Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppColors.Border),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Deal Validity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Active", color = AppColors.BrandOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        DealDetailRow("Valid from", formatIsoDate(currentDeal.availableFrom) ?: "-")
                        DealDetailRow("Valid until", formatIsoDate(currentDeal.availableUntil) ?: "-")
                        
                        val daysTotal = DateUtils.daysBetween(currentDeal.availableFrom, currentDeal.availableUntil).coerceAtLeast(1)
                        val daysLeft = DateUtils.getDaysRemaining(currentDeal.availableUntil) ?: 0
                        val progress = (daysLeft.toFloat() / daysTotal.toFloat()).coerceIn(0f, 1f)
                        
                        DealDetailRow("Time remaining", if(daysLeft > 0) "$daysLeft days ${24 - java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)} hours" else "Expired", isRed = daysLeft <= 3)

                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = AppColors.BrandOrange,
                            trackColor = AppColors.Bg,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Deal expires soon", style = MaterialTheme.typography.bodySmall, color = AppColors.MutedText)
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                // 4. Product Details Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AppColors.Border),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Product Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        // Fake data for fields not in Deal model
                        DealDetailRow("Brand", currentDeal.store) // Placeholder
                        DealDetailRow("Category", currentDeal.category)
                        DealDetailRow("Deal ID", "#${currentDeal.id.take(8)}")
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                // 5. Store Information Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AppColors.Border),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Store Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(AppColors.Red50, RoundedCornerShape(12.dp)), // Store Icon
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Storefront, null, tint = AppColors.Danger)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(currentDeal.store, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Department Store", color = AppColors.MutedText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        
                        // Fake Store rows based on design
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.LocationOn, null, tint = AppColors.MutedText, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Find nearest store", modifier = Modifier.weight(1f), color = AppColors.BrandInk)
                            Text("Locate", color = AppColors.BrandOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.AccessTime, null, tint = AppColors.MutedText, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Store hours: 8AM - 10PM", modifier = Modifier.weight(1f), color = AppColors.BrandInk)
                        }
                        Spacer(Modifier.height(12.dp))
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Phone, null, tint = AppColors.MutedText, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Customer service", modifier = Modifier.weight(1f), color = AppColors.BrandInk)
                            Text("Call", color = AppColors.BrandOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
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
