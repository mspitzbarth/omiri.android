package com.example.omiri.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.outlined.Delete
import com.example.omiri.ui.theme.AppColors

@Composable
fun ContextualSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onEdit: () -> Unit // Only enabled if selectedCount == 1
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(horizontal = com.example.omiri.ui.theme.Spacing.lg)
        ) {
            Spacer(Modifier.height(com.example.omiri.ui.theme.Spacing.xxs))

            // Top bar row matching OmiriHeader
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Navigation Icon & Title (Left)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    // Use Box/Icon with clickable instead of IconButton to remove internal padding and align "X" to the left edge like the Logo
                    Box(
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                        modifier = Modifier
                            .size(32.dp) // Smaller touch target wrapper than 48dp to align better visually? Or just Icon? 
                            // Let's use Icon with padding for touch area but less visual indent
                            .padding(end = 6.dp) // Adjustment
                            .clickable(onClick = onClearSelection)
                    ) {
                         Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Selection",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = Color(0xFF111827)
                    )
                }

                // Actions (Right)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    // Trash Icon (Circular)
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .clickable(onClick = onDelete)
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // More Menu (Circular)
                    Box {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .clickable(onClick = { showMenu = true })
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.MoreVert, // No Outlined for MoreVert usually
                                    contentDescription = "More", 
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (selectedCount == 1) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Duplicate", color = Color(0xFF374151)) },
                                leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Move to List", color = Color(0xFF374151)) },
                                leadingIcon = { Icon(Icons.Outlined.DriveFileMove, null) }, 
                                onClick = {
                                    showMenu = false
                                    onMove()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(com.example.omiri.ui.theme.Spacing.xxs))
        }
    }
}
