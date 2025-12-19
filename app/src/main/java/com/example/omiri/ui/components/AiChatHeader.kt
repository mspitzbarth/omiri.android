package com.example.omiri.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.ui.theme.Spacing

@Composable
fun AiChatHeader(
    onBackClick: () -> Unit,
    isOnline: Boolean,
    onMenuClick: () -> Unit = {}, // Optional now if handled internally, but keeping for compatibility
    onClearChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
         Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
            Spacer(Modifier.height(Spacing.xxs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Left)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                     Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(Spacing.sm))

                // Title and Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OMIRI Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isOnline) Color(0xFF10B981) else Color(0xFF9CA3AF), 
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) Color(0xFF10B981) else Color(0xFF6B7280)
                        )
                    }
                }

                // Menu Action (Right)
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Delete chat", 
                                    color = Color(0xFFEF4444) // Red color for delete action
                                ) 
                            },
                            onClick = {
                                menuExpanded = false
                                onClearChatClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xxs))
         }
    }
}
