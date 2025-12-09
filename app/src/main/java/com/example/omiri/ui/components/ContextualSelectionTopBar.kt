package com.example.omiri.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                    // Trash Icon
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete, // Or Filled? User said "same for the selection icon". Maybe Filled Delete? Material usually uses Outlined or Filled. "selection icon" might refer to X? I'll stick to Outlined for actions unless "all icons" meant these too. Let's make Delete Filled to be safe? Or stick to Outlined but smaller/colored. User said "all icons in the header nav... same for the selection icon". I'll use Filled for consistency if available, otherwise Outlined. Delete has Filled. 
                            // Let's use Outlined for generic actions usually, but user said "filled" for header nav.
                            // I will use Filled to be safe given "make all icons... filled".
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // More Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert, // Filled
                                contentDescription = "More", 
                                tint = Color(0xFF1F2937),
                                modifier = Modifier.size(20.dp)
                            )
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
