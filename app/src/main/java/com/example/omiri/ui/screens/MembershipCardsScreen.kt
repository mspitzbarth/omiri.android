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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.data.models.MembershipCard
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.MembershipCardViewModel
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
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Cards & Memberships", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardSheet = true },
                containerColor = Color(0xFFEA580B),
                contentColor = Color.White
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Card")
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No cards yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Button(
                        onClick = { showAddCardSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580B))
                    ) {
                        Text("Add your first card")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(cards, key = { it.id }) { card ->
                    MembershipCardItem(
                        card = card,
                        onClick = { selectedCard = card },
                        onDelete = { viewModel.deleteCard(card.id) }
                    )
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
fun MembershipCardItem(
    card: MembershipCard,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
        onClick = onClick
    ) {
        Column {
            // Card Image Header (if exists)
            card.imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            // Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    if (card.cardNumber.isNotBlank()) {
                         Text(
                            text = card.cardNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
