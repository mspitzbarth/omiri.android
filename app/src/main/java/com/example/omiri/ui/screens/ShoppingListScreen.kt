package com.example.omiri.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.components.OmiriSearchBar
import com.example.omiri.ui.components.ShoppingListItem
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ShoppingListViewModel

import com.example.omiri.ui.components.simpleVerticalScrollbar
import androidx.compose.foundation.lazy.rememberLazyListState

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(),
    onNotificationsClick: () -> Unit = {}
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val currentListId by viewModel.currentListId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentList by viewModel.currentList.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            com.example.omiri.ui.components.OmiriHeader(
                notificationCount = 2,
                onNotificationClick = onNotificationsClick
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                ) {
                    Spacer(Modifier.height(Spacing.md))

                    // List name with selection sheet
                    var showListSelectionSheet by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showListSelectionSheet = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentList?.name ?: "My List",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Switch list",
                            tint = Color(0xFF6B7280)
                        )
                    }

                    if (showListSelectionSheet) {
                        com.example.omiri.ui.components.ListSelectionBottomSheet(
                            shoppingLists = shoppingLists,
                            currentListId = currentListId,
                            onListSelected = { listId ->
                                viewModel.switchList(listId)
                                showListSelectionSheet = false
                            },
                            onCreateListClick = {
                                showListSelectionSheet = false
                                showCreateListDialog = true
                            },
                            onDeleteList = { listId ->
                                viewModel.deleteList(listId)
                            },
                            onResetList = {
                                viewModel.resetRecurringItems()
                            },
                            onDismiss = { showListSelectionSheet = false }
                        )
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // Search bar
                    OmiriSearchBar(
                        value = searchQuery,
                        placeholder = "Search items in your list…",
                        onQueryChange = { viewModel.updateSearchQuery(it) }
                    )

                    Spacer(Modifier.height(Spacing.lg))
                    
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }

            // Items list
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.lg)
                    .simpleVerticalScrollbar(listState),
                contentPadding = PaddingValues(bottom = Spacing.xxxl)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    ShoppingListItem(
                        item = item,
                        onToggleDone = {
                            viewModel.toggleItemDone(item.id)
                        },
                        onDelete = {
                            viewModel.deleteItem(item.id)
                        },
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) {
                                    "No items yet. Tap + to add!"
                                } else {
                                    "No items found"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }

        // Floating action button
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
                .padding(bottom = 56.dp),
            containerColor = Color(0xFFEA580B),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add item"
            )
        }
    }

    // Dialogs
    if (showAddItemDialog) {
        com.example.omiri.ui.components.AddItemBottomSheet(
            onDismiss = { showAddItemDialog = false },
            onAdd = { itemName, categoryId, isRecurring ->
                viewModel.addItem(itemName, categoryId, isInDeals = false, isRecurring = isRecurring)
                showAddItemDialog = false
            }
        )
    }

    if (showCreateListDialog) {
        com.example.omiri.ui.components.CreateListBottomSheet(
            onDismiss = { showCreateListDialog = false },
            onCreate = { name ->
                viewModel.createList(name)
                showCreateListDialog = false
            }
        )
    }
}
