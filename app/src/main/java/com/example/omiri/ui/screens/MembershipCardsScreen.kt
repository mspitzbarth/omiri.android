package com.example.omiri.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.data.models.MembershipCard
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MembershipCardViewModel
import com.example.omiri.ui.components.ScreenHeader
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipCardsScreen(
    onBackClick: () -> Unit = {},
    viewModel: MembershipCardViewModel = viewModel()
) {
    val cards by viewModel.cards.collectAsState(initial = emptyList())
    // Sheet State
    var showAddCardSheet by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<MembershipCard?>(null) }
    
    // Filter State (Visual only for now)
    var selectedFilter by remember { mutableStateOf("All") }
    
    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Cards & Memberships",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardSheet = true },
                containerColor = Color(0xFFFE8357),
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Card", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar Placeholder
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF3F4F6),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Search stores, cards...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Filters
            val filters = listOf("All", "Favorites", "Nearby", "Expiring Soon")
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = Spacing.sm)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFE8357),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF1F2937)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            
            // Summary Chips (Mock Data)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val summaryStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                    Text("${cards.size} cards", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = summaryStyle)
                }
                Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(4.dp)) {
                    Text("3 favorites", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = summaryStyle, color = Color(0xFFEA580C))
                }
            }
            
            HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = Spacing.sm))

            if (cards.isEmpty()) {
                com.example.omiri.ui.components.OmiriEmptyState(
                    icon = Icons.Outlined.CreditCard,
                    title = "No cards yet",
                    message = "Add your loyalty and membership cards to access them quickly",
                    buttonText = "Add your first card",
                    onButtonClick = { showAddCardSheet = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = 80.dp), // Space for FAB
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(cards, key = { it.id }) { card ->
                        MembershipCardItemNew(
                            card = card,
                            onClick = { selectedCard = card },
                            onDelete = { viewModel.deleteCard(card.id) }
                        )
                    }
                }
            }
        }
        
        // Show Add Card Sheet
        if (showAddCardSheet) {
            com.example.omiri.ui.components.AddMembershipCardBottomSheet(
                onDismiss = { showAddCardSheet = false },
                onSave = { showAddCardSheet = false }
            )
        }

        // Show Details Sheet
        if (selectedCard != null) {
            com.example.omiri.ui.components.MembershipCardDetailsBottomSheet(
                card = selectedCard!!,
                onDismiss = { selectedCard = null },
                onDelete = { 
                    viewModel.deleteCard(selectedCard!!.id)
                    selectedCard = null
                },
                storeName = null 
            )
        }
    }
}

@Composable
fun MembershipCardItemNew(
    card: MembershipCard,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Determine Color
    val cardColor = try {
        Color(android.graphics.Color.parseColor(card.colorHex.ifEmpty { "#FF9900" }))
    } catch (e: Exception) {
        // Fallback colors based on name if hex is invalid
        when(card.name.lowercase()) {
            "target" -> Color(0xFFE83636)
            "walmart" -> Color(0xFF2C7AFA)
            "home depot" -> Color(0xFFF97316)
            "planet fitness" -> Color(0xFF9333EA)
            "starbucks" -> Color(0xFF22C55E)
            "costco" -> Color(0xFF0D4F97) // Costco Blue
            else -> Color(0xFFFE8357) // Default Orange
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Decoration (Circles/Waves) can be added here
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Try to find a specific icon or just use first letter
                                Text(
                                    text = card.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor
                                )
                            }
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = card.tier ?: "Rewards Member",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Top Right Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Star, contentDescription = "Favorite", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.QrCode, contentDescription = "QR", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                
                // Middle: Member ID
                Column {
                    Text(
                        text = "Member ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "•••• •••• ${card.cardNumber.takeLast(4)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
                
                // Bottom: Tags
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val tierTag = card.tier ?: "Rewards"
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = tierTag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Digital",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
