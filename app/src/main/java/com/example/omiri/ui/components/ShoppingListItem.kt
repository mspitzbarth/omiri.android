package com.example.omiri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.ShoppingItem
import com.example.omiri.ui.theme.Spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)), // Light gray border
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onToggleDone() },
                onLongClick = { onEdit() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat style as per image (shadow is subtle/handled by border often in modern flat UI)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Generous padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Checkbox (Left)
            // Using Box to ensure touch target but visual square
            Box(
                modifier = Modifier.padding(end = 12.dp)
            ) {
                androidx.compose.material3.Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { onToggleDone() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFEA580B), // Orange Branding
                        uncheckedColor = Color(0xFFD1D5DB), // Gray outline
                        checkmarkColor = Color.White
                    )
                )
            }

            // 2. Info (Middle)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = if (item.isDone) Color(0xFF9CA3AF) else Color(0xFF111827), // Gray if done, Dark if not
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Badges Row (Category • Store)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category
                    Text(
                        text = item.category.getName(), 
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280) // Gray text
                    )
                    
                    if (item.store != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "•", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        // Store Badge
                        Surface(
                            color = Color(0xFFDBEAFE), // Blue 100
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.store,
                                color = Color(0xFF1D4ED8), // Blue 700
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.isRecurring) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Recurring",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // 3. Right Side (Price/Badge/Actions)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                 // Price Display
                 if (item.discountPrice != null || item.price != null) {
                     if (item.discountPrice != null && (item.price != null && item.discountPrice < item.price)) {
                         // Discounted State
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             if (item.discountPercentage != null) {
                                 Text(
                                     text = "-${item.discountPercentage}%",
                                     style = MaterialTheme.typography.labelSmall,
                                     color = Color(0xFFDC2626), // Red
                                     fontWeight = FontWeight.Bold
                                 )
                                 Spacer(modifier = Modifier.width(4.dp))
                             }
                             
                             Text(
                                 text = "€${String.format("%.2f", item.discountPrice)}",
                                 style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                 color = Color(0xFFEA580B) // Orange for deal price
                             )
                         }
                         if (item.price != null) {
                             Text(
                                 text = "€${String.format("%.2f", item.price)}",
                                 style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.LineThrough),
                                 color = Color(0xFF9CA3AF)
                             )
                         }
                     } else {
                         // Regular Price
                         item.price?.let {
                             Text(
                                 text = "€${String.format("%.2f", it)}",
                                 style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                 color = Color(0xFF111827)
                             )
                         }
                     }
                 } else if (item.isInDeals) {
                     // Fallback Deal Badge if no specific price
                     Surface(
                         color = Color(0xFFDCFCE7), // Light Green
                         shape = RoundedCornerShape(16.dp)
                     ) {
                         Text(
                             text = "On Deal",
                             color = Color(0xFF166534), // Dark Green
                             style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                             modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                         )
                     }
                 }
                 
                 // Delete Button
                 IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(top = 8.dp)) {
                     Icon(
                         imageVector = Icons.Outlined.Delete, 
                         contentDescription = "Delete", 
                         tint = Color(0xFFD1D5DB)
                     )
                 }
            }
        }
    }
}
