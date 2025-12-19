package com.example.omiri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.omiri.R
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.ui.theme.Spacing

@Composable
fun AiChatEmptyState(
    onActionClick: (String) -> Unit,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.lg))

        // 1. Hero Card
        AiAssistantHeroCard(isOnline = isOnline)

        Spacer(Modifier.height(32.dp))
        
        // 2. Bot Intro
        BotIntroMessage(isOnline = isOnline)
        
        Spacer(Modifier.height(24.dp))
        
        // 4. Try asking
        Text(
            text = "Try asking:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(12.dp))
        
        SuggestedQueriesList(onActionClick)
        
        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun AiAssistantHeroCard(isOnline: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Light Orange/Beige
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with status dot
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFFE8357), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                     Icon(
                        painter = painterResource(id = R.drawable.ic_omiri_logo),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Status dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Hi! I'm your shopping assistant. I can help you find deals, organize lists, suggest recipes, and plan your shopping trips. What would you like to do today?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Features Grid (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                 Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                     FeatureItem("Find best deals")
                     FeatureItem("Create lists")
                 }
                 Spacer(Modifier.width(24.dp))
                 Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                     FeatureItem("Plan routes")
                     FeatureItem("Get recipes")
                 }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            tint = Color(0xFFFE8357),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
    }
}

@Composable
private fun BotIntroMessage(isOnline: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Box {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFE8357), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    painter = painterResource(id = R.drawable.ic_omiri_logo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Status dot for message avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(8.dp)
                    .background(Color.White, CircleShape)
                    .padding(1.dp)
                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(horizontalAlignment = Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = "Hello! I am your AI shopping assistant. How can I help you prepare for your next trip today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Just now",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SuggestedQueriesList(onClick: (String) -> Unit) {
    val queries = listOf(
        "What's the best route to save time today?",
        "Show me items expiring soon",
        "Find recipes with chicken and vegetables",
        "What are the biggest deals this week?"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        queries.forEach { query ->
             Surface(
                 onClick = { onClick(query) },
                 shape = RoundedCornerShape(12.dp),
                 border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                 color = Color.White,
                 modifier = Modifier.fillMaxWidth()
             ) {
                 Text(
                     text = "\"" + query + "\"", // Add quotes as in image
                     style = MaterialTheme.typography.bodyMedium,
                     color = Color(0xFF4B5563),
                     fontWeight = FontWeight.Medium,
                     modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                 )
             }
        }
    }
}
