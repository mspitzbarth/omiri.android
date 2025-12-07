package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.ShoppingList
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSelectionBottomSheet(
    shoppingLists: List<ShoppingList>,
    currentListId: String?,
    onListSelected: (String) -> Unit,
    onCreateListClick: () -> Unit,
    onDeleteList: (String) -> Unit,
    onResetList: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null, // Remove default handle for floating look, or keep if preferred. Let's remove for cleaner float.
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
                .navigationBarsPadding() // Layout above nav bar
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.lg)
                ) {
                    // Header with Drag Handle indicator purely visual or just title
                    // Adding a small visual handle inside the card looks nice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(width = 32.dp, height = 4.dp),
                            color = Color(0xFFE5E7EB),
                            shape = CircleShape
                        ) {}
                    }

                    Text(
                        text = "My Lists",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    )

                    Spacer(Modifier.height(Spacing.md))

                    LazyColumn {
                        items(shoppingLists) { list ->
                            val isSelected = list.id == currentListId
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = list.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFEA580B) else Color(0xFF111827)
                                    )
                                },
                                supportingContent = {
                                    Text("${list.totalItems} items")
                                },
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFFEA580B),
                                                modifier = Modifier.padding(end = Spacing.sm)
                                            )
                                        }
                                        if (shoppingLists.size > 1) {
                                            IconButton(onClick = { onDeleteList(list.id) }) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete list",
                                                    tint = Color(0xFF9CA3AF)
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.clickable {
                                    onListSelected(list.id)
                                    onDismiss()
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                        }

                        item {
                            ListItem(
                                headlineContent = { Text("Create New List") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580B)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onCreateListClick()
                                    onDismiss()
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                    headlineColor = Color(0xFFEA580B)
                                )
                            )
                        }

                        item {
                            ListItem(
                                headlineContent = { Text("Reset Auto-Items") },
                                supportingContent = { Text("Uncheck recurring items") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280)
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onResetList()
                                    onDismiss()
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
