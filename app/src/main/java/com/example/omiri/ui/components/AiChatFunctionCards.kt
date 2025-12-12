package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.theme.AppColors

// 1. app-shopping_list_add -> Shopping List Update
@Composable
fun ShoppingListUpdateCard(data: Map<String, Any>, onViewList: () -> Unit) {
    val items = data["items"] as? List<String> ?: emptyList()
    val addedCount = data["addedCount"] ?: items.size
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp)), // Green-100
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.List, null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Shopping List Update", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                    Text("Added: $addedCount items", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
                }
                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(100.dp)) {
                    Text("Synced", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF16A34A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Items
            val displayItems = items.take(4) // Show 4 max as per image (3 + 1)
            val remaining = if (items.size > 4) items.size - 4 else 0
            
            displayItems.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(item, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF374151), fontWeight = FontWeight.Medium)
                }
            }
            
            if (remaining > 0) {
                 Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(32.dp))
                    Text("+$remaining more", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onViewList,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("View List", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF374151)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Add More", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 2. app-products_search -> "Found Deals" (Image 4)
@Composable
fun FoundDealsCard(data: Map<String, Any>) {
    val query = data["query"] as? String ?: ""
    val items = data["items"] as? List<Map<String, String>> ?: emptyList()
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFDBEAFE), CircleShape), // Blue-100
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.LocalOffer, null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Found Deals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                    Text("for \"$query\"", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // List Items
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item Icon (Cart)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item["name"] ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(item["store"] ?: "", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
                    }
                    
                    // Price Badge
                    Surface(
                        color = Color(0xFFDBEAFE), // Light Blue
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = item["price"] ?: "",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF1E40AF), // Dark Blue
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            }
        }
    }
}

// 3. app-recipe_search -> "Recipe Ideas" (Image 3)
@Composable
fun RecipeIdeasCard(data: Map<String, Any>) {
    val recipes = data["recipes"] as? List<Map<String, String>> ?: emptyList()
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFFF3E8FF), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.RestaurantMenu, null, tint = Color(0xFF9333EA), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Recipe Ideas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                    Text("Using your ingredients", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
                }
                Surface(color = Color(0xFFF3E8FF), shape = RoundedCornerShape(100.dp)) {
                    Text("Top Picks", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF9333EA), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Grid/Row of Recipes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                recipes.take(2).forEach { recipe ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFFFF7ED), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp) // Taller as per image
                                .background(
                                    if(recipe["color"] == "orange") Color(0xFFFFCCBC).copy(alpha=0.5f) else Color(0xFFFFE082).copy(alpha=0.5f), 
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                             val emoji = if(recipe["color"] == "orange") "🍝" else "🍕"
                             Text(emoji, fontSize = 32.sp)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Text(recipe["name"] ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color(0xFF1F2937))
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                             Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                 Text(recipe["difficulty"] ?: "Easy", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF166534), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                             }
                             Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(6.dp)) {
                                 Text(recipe["time"] ?: "20 min", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF1E40AF), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                             }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Cook This", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. app-find_best_stores -> "Recommended Store Run" (Image 2)
@Composable
fun RecommendedStoreRunCard(data: Map<String, Any>) {
    val stores = data["stores"] as? List<Map<String, Any>> ?: emptyList()
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), // Light Blue background per image
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFF2563EB), RoundedCornerShape(12.dp)), // Blue Square
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Map, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("Recommended Store Run", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Store List
            stores.forEach { store -> 
                val name = store["name"] as? String ?: ""
                val items = store["items"] as? String ?: "" // e.g., "6 items"
                val deals = store["deals"] as? String ?: "" // e.g., "3 deals"
                val colorStr = store["color"] as? String ?: "blue"
                val isActive = (store["active"] as? Boolean) ?: true
                
                val iconColor = when(colorStr) {
                    "red" -> Color(0xFFEF4444)
                    "green" -> Color(0xFF22C55E)
                    else -> Color(0xFF2563EB)
                }

                Row(
                   modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                   verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Store Icon
                         Box(
                            modifier = Modifier.size(40.dp).background(iconColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                         ) {
                             Icon(Icons.Outlined.Storefront, null, tint = Color.White, modifier = Modifier.size(20.dp))
                         }
                         Spacer(Modifier.width(12.dp))
                         
                         Text(name, style = MaterialTheme.typography.titleMedium, color = Color(0xFF374151))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(items, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                        Spacer(Modifier.width(12.dp))
                        
                        // Badge
                        Surface(
                            color = if(isActive) Color(0xFFFE8357) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                text = deals,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = if(isActive) Color.White else Color(0xFF374151),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. app-deals_match_list -> "Deals Matched" (Image 0/Original)
@Composable
fun DealsMatchedCard(data: Map<String, Any>) {
     val items = data["items"] as? List<Map<String, String>> ?: emptyList()
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color(0xFFFFEDD5), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.LocalOffer, null, tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Deals Matched to Your List", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827))
                    Text("${data["count"]} items on sale", color = Color(0xFF6B7280), style = MaterialTheme.typography.bodyMedium)
                }
                Surface(color = Color(0xFFFE8357), shape = RoundedCornerShape(100.dp)) {
                    Text(data["badge"]?.toString() ?: "Best Saves", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(
                        when(item["icon"]) {
                            "wheat" -> Color(0xFFFEF08A) // Yellow
                            "meat" -> Color(0xFFFECACA) // Red
                            else -> Color(0xFFFFE4E6) // Pink
                        }, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                         
                         val iconVec = when(item["icon"]) {
                             "wheat" -> Icons.Outlined.LocalDining // Fallback from Grass
                             "meat" -> Icons.Outlined.Restaurant
                             else -> Icons.Outlined.LocalDining
                         }
                         // Tint is subtle version of bg or dark
                         val tint = when(item["icon"]) {
                             "wheat" -> Color(0xFFA16207)
                             "meat" -> Color(0xFFB91C1C)
                             else -> Color(0xFFBE185D)
                         }
                         Icon(iconVec, null, tint = tint, modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(Modifier.weight(1f)) {
                        Text(item["name"] ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF111827))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item["price"] ?: "", color = Color(0xFFFE8357), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(item["oldPrice"] ?: "", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.bodySmall, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            Spacer(Modifier.width(8.dp))
                            Surface(color = Color(0xFFFFEDD5), shape = RoundedCornerShape(4.dp)) {
                                Text(item["discount"] ?: "", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFF97316), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("View All Deals", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF374151)),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Add to Cart", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. Suggestions Row
@Composable
fun SuggestionsRow(data: Map<String, Any>, onSuggestionClick: (String) -> Unit) {
    val suggestions = data["suggestions"] as? List<String> ?: emptyList()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Spacing between chips
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                onClick = { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(100.dp), // Fully rounded pills
                color = Color(0xFFF3F4F6),
                modifier = Modifier.weight(1f) // Distribute evenly
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
