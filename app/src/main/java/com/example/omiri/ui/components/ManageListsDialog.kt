package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.omiri.data.models.ShoppingList
import com.example.omiri.ui.theme.Spacing

@Composable
fun ManageListsDialog(
    isVisible: Boolean,
    lists: List<ShoppingList>,
    currentListId: String?,
    onDismiss: () -> Unit,
    onCreateList: (String) -> Unit,
    onSelectList: (String) -> Unit,
    onDeleteList: (String) -> Unit
) {
    if (!isVisible) return

    var showCreateDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(Spacing.md),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Shopping Lists",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { showCreateDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create new list",
                            tint = Color(0xFFFE8357)
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Lists
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(lists) { list ->
                        ListItem(
                            list = list,
                            isSelected = list.id == currentListId,
                            onSelect = {
                                onSelectList(list.id)
                                onDismiss()
                            },
                            onDelete = {
                                if (lists.size > 1) {
                                    onDeleteList(list.id)
                                }
                            },
                            canDelete = lists.size > 1
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.md))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFE8357),
                        contentColor = Color.White
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }

    // Create list dialog
    if (showCreateDialog) {
        CreateListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                onCreateList(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ListItem(
    list: ShoppingList,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFFEF3C7) else Color(0xFFF9FAFB)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = Color(0xFFFE8357),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF111827),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "${list.totalItems} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete list",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateListDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var listName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = "Create New List",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827)
                )

                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter list name...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE8357),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = {
                            listName = ""
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (listName.isNotBlank()) {
                                onCreate(listName)
                                listName = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = listName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFE8357),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
