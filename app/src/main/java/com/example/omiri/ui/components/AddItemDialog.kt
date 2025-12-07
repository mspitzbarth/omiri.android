package com.example.omiri.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.omiri.data.models.PredefinedCategories
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    if (!isVisible) return

    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PredefinedCategories.OTHER) }
    var expanded by remember { mutableStateOf(false) }

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
                    text = "Add Item",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter item name...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEA580B),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.getName("en"),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Select category",
                                    tint = Color(0xFF6B7280)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEA580B),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            PredefinedCategories.ALL_CATEGORIES.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(category.color, RoundedCornerShape(6.dp))
                                            )
                                            Text(
                                                text = category.getName("en"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF111827)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = MenuDefaults.itemColors(
                                        textColor = if (selectedCategory.id == category.id)
                                            Color(0xFFEA580B) else Color(0xFF111827)
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = {
                            itemName = ""
                            selectedCategory = PredefinedCategories.OTHER
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
                            if (itemName.isNotBlank()) {
                                onAdd(itemName, selectedCategory.id)
                                itemName = ""
                                selectedCategory = PredefinedCategories.OTHER
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = itemName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA580B),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
