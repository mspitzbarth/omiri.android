package com.example.omiri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.ui.components.OmiriHeader
import com.example.omiri.ui.theme.Spacing

@Composable
fun RecipesScreen(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            OmiriHeader(
                onNotificationClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFFF9FAFB) // App Colors Bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item { Spacer(Modifier.height(Spacing.sm)) }
            
            // Search Bar
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Search, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text("Search recipes, ingredients...", color = Color.Gray, modifier = Modifier.weight(1f))
                        Icon(Icons.Outlined.Mic, null, tint = Color.Gray)
                    }
                }
            }

            // Cook from your list Card
            item {
                Card(
                     shape = RoundedCornerShape(16.dp),
                     colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light Orange
                     border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                             Box(
                                 modifier = Modifier.size(40.dp).background(Color(0xFFFE8357), RoundedCornerShape(8.dp)),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(Icons.Outlined.RestaurantMenu, null, tint = Color.White)
                             }
                             Spacer(Modifier.width(12.dp))
                             Column {
                                 Text("Cook from your list", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                 Text("You already have 6 ingredients for these recipes.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                             }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("See best matches")
                        }
                    }
                }
            }
            
            // Filters Row (Chips)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = true, label = "For You", onClick = {})
                    FilterChip(selected = false, label = "From My List", onClick = {})
                    FilterChip(selected = false, label = "Matched Deals", onClick = {})
                }
            }

            // Best Matches
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Best Matches", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("View All >", color = Color(0xFFFE8357), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            item {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                
                @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                val flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    lazyListState = listState,
                    snapPosition = androidx.compose.foundation.gestures.snapping.SnapPosition.Start
                )

                LazyRow(
                    state = listState,
                    flingBehavior = flingBehavior,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    contentPadding = PaddingValues(horizontal = Spacing.md), // Add padding for snap effect
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(3) {
                         RecipeCard(
                             title = "Grilled Chicken & Garden Salad",
                             time = "25 min",
                             servings = "4 servings",
                             tags = listOf("High Protein", "Uses your list"),
                             buttonText = "Cook This",
                             width = 280.dp
                         )
                    }
                }
            }
            
            // Deal-Friendly Recipes
            item {
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Deal-Friendly Recipes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("View All >", color = Color(0xFFFE8357), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            items(3) {
                 RecipeListItem(
                     title = "Homemade Margherita Pizza",
                     time = "35 min",
                     servings = "4",
                     savedAmount = "€5.20",
                     store = "Lidl & Aldi"
                 )
            }
            
            item { Spacer(Modifier.height(80.dp)) } // Bottom padding
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if(selected) Color(0xFFFE8357) else Color.White,
        border = if(!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if(selected) Color.White else Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecipeCard(
    title: String,
    time: String,
    servings: String,
    tags: List<String>,
    buttonText: String,
    width: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier.width(width),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(140.dp)
                     .background(Color.Gray) // Placeholder for image
            ) {
                 // Image placeholder
                 Surface(
                     modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                     color = Color(0xFFDCFCE7),
                     shape = RoundedCornerShape(4.dp)
                 ) {
                     Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                         // Check icon
                         Text("5 items", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                     }
                 }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Outlined.Group, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(servings, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                            Text(tag, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if(tag == "Uses your list") Color(0xFFFE8357) else Color(0xFF166534))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8357)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun RecipeListItem(
    title: String,
    time: String,
    servings: String,
    savedAmount: String,
    store: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.Group, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(servings, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFFFEDD5), shape = RoundedCornerShape(4.dp)) {
                        Text("Matched Deals", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFFFE8357), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Save $savedAmount", style = MaterialTheme.typography.labelSmall, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text("Best at $store", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
