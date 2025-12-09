package com.example.omiri.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.omiri.data.models.PredefinedCategories
import com.example.omiri.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, Boolean) -> Unit,
    initialItem: com.example.omiri.data.models.ShoppingItem? = null,
    modifier: Modifier = Modifier
) {
    var itemName by remember { mutableStateOf(initialItem?.name ?: "") }
    var selectedCategory by remember { 
        mutableStateOf(initialItem?.category ?: PredefinedCategories.OTHER) 
    }
    var isRecurring by remember { mutableStateOf(initialItem?.isRecurring ?: false) }
    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Voice Search Launcher
    val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText: String? = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (spokenText != null) {
                itemName = spokenText
            }
        }
    }

    // Auto-focus the text field when the sheet opens (only if adding, or just always nice)
    // If editing, maybe don't pop keyboard immediately? Let's keep it for convenience.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent, // Transparent for floating effect
        dragHandle = null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
                .navigationBarsPadding() // Handled here for the floating card
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
                        .padding(Spacing.lg)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (initialItem != null) "Edit Item" else "Add Item",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(Modifier.height(Spacing.md))

                    // Input Field
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("What do you need?") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak item name...")
                                    }
                                    try {
                                        voiceLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Voice input",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEA580B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = MaterialTheme.shapes.large,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (itemName.isNotBlank()) {
                                    onAdd(itemName, selectedCategory.id, isRecurring)
                                    // Don't clear if editing, logic handles dismiss
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Categories Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.getName("en"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(selectedCategory.color, CircleShape)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEA580B),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB)
                            ),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
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
                                                    .background(category.color, CircleShape)
                                            )
                                            Text(category.getName("en"))
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }


                    
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Recurring Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRecurring = !isRecurring }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEA580B),
                                uncheckedThumbColor = Color(0xFFD1D5DB),
                                uncheckedTrackColor = Color(0xFFF3F4F6)
                            )
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column {
                            Text(
                                text = "Recurring Item",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Reset this item automatically",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    // Add/Save Button
                    Button(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                onAdd(itemName, selectedCategory.id, isRecurring)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEA580B),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = itemName.isNotBlank()
                    ) {
                        Text(
                            text = if (initialItem != null) "Save Changes" else "Add to List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

