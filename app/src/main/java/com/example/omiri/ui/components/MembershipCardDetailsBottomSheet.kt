package com.example.omiri.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.data.models.MembershipCard
import com.example.omiri.ui.theme.Spacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipCardDetailsBottomSheet(
    card: MembershipCard,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    storeName: String? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Image
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        card.imagePath?.let { path ->
                            val file = File(path)
                            if (file.exists()) {
                                val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } ?: run {
                            // Placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFF7ED)), // Light orange
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Store,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580B),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        
                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(Spacing.md)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    // Content
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        // Title & Store
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                if (!storeName.isNullOrBlank()) {
                                    Text(
                                        text = storeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFEA580B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        Divider(color = Color(0xFFF3F4F6))
                        
                        // Card Code
                        if (card.cardNumber.isNotBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Card Number",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(Modifier.height(Spacing.sm))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.lg),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.QrCode, 
                                            contentDescription = null,
                                            tint = Color(0xFF374151)
                                        )
                                        Spacer(Modifier.width(Spacing.md))
                                        Text(
                                            text = card.cardNumber,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            letterSpacing = 2.sp,
                                            color = Color(0xFF111827)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(Spacing.sm))
                        
                        // Delete Button
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEE2E2),
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Delete Card", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
