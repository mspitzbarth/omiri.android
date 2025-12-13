package com.example.omiri.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omiri.ui.components.RecommendedStoreRunCard
import com.example.omiri.ui.components.ShoppingListItem
import com.example.omiri.ui.components.simpleVerticalScrollbar
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.viewmodels.ShoppingListViewModel
import com.example.omiri.viewmodels.ProductViewModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(),
    productViewModel: ProductViewModel? = null,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val currentListId by viewModel.currentListId.collectAsState()
    val currentList by viewModel.currentList.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterStoreName by viewModel.filterStoreName.collectAsState()
    
    // Selection Mode State
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    
    // Smart Plan
    val smartPlan by (productViewModel?.smartPlan ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showMoveSelectionSheet by remember { mutableStateOf(false) }
    var showListSelectionSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.example.omiri.data.models.ShoppingItem?>(null) }
    
    // Header Stats (Mocked or calculated)
    val totalItemsCount = currentList?.items?.size ?: 0
    val matchedDealsCount = filteredItems.count { it.isInDeals || it.discountPrice != null }
    val savedAmount = 12.40 // Mocked per image, or calculate if data available

    Box(
        modifier = Modifier.fillMaxSize().background(com.example.omiri.ui.theme.AppColors.Bg) // Ensure background is light gray so white items pop
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header (Notification/Profile OR Contextual Selection Bar)
            if (inSelectionMode) {
                com.example.omiri.ui.components.ContextualSelectionTopBar(
                    selectedCount = selectedItemIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDelete = { viewModel.deleteSelectedItems() },
                    onDuplicate = { 
                         // Duplicate items
                         val count = selectedItemIds.size
                         viewModel.duplicateSelectedItems()
                         scope.launch {
                             snackbarHostState.showSnackbar("Duplicated $count items")
                         }
                    },
                    onMove = {
                        showMoveSelectionSheet = true
                    },
                    onEdit = {
                        // Only called if 1 item selected
                        val id = selectedItemIds.firstOrNull()
                        if (id != null) {
                            val item = filteredItems.find { it.id == id }
                            if (item != null) {
                                itemToEdit = item
                                showAddItemDialog = true
                                viewModel.clearSelection()
                            }
                        }
                    }
                )
            } else {
                com.example.omiri.ui.components.OmiriHeader(
                    notificationCount = 2,
                    onNotificationClick = onNotificationsClick,
                    onProfileClick = onProfileClick
                )
            }

            // Content List
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .simpleVerticalScrollbar(listState),
                contentPadding = PaddingValues(bottom = 180.dp)
            ) {
                // Screen Header Section
                item {
                    Surface(
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.lg)
                                .padding(top = Spacing.md, bottom = Spacing.md)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { showListSelectionSheet = true }
                            ) {
                                Text(
                                    text = currentList?.name ?: "Weekly Groceries",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "Switch List",
                                    tint = Color(0xFF6B7280)
                                )
                            }
                            
                            Spacer(Modifier.height(4.dp))
                            
                            // Stats Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$totalItemsCount items • $matchedDealsCount matched deals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = "You saved €${String.format("%.2f", savedAmount)} this week",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF16A34A), // Green
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Store Filter Chip removed as per request - handled inside RecommendedStoreRunCard visual state

                        // All Chip (Active if selectedCategory is null AND filterStore is null)
                        val isAllSelected = selectedCategory == null && filterStoreName == null
                        Box(modifier = Modifier.clickable { viewModel.selectCategory(null) }) {
                            FilterChipStub(
                                text = "All ($totalItemsCount)", 
                                selected = isAllSelected, 
                                color = Color(0xFFFE8357)
                            )
                        }
                        
                        // Dynamic Categories
                        availableCategories.forEach { category ->
                            val isSelected = selectedCategory == category.id
                            Box(modifier = Modifier.clickable { viewModel.selectCategory(if (isSelected) null else category.id) }) {
                                FilterChipStub(
                                    text = "${category.name} (${category.count})", 
                                    selected = isSelected,
                                    color = Color(0xFFFE8357)
                                )
                            }
                        }
                    }
                }
                
                // Recommended Store Run
                item {
                    if (smartPlan != null) {
                        RecommendedStoreRunCard(
                            plan = smartPlan!!,
                            selectedStore = filterStoreName,
                            containerColor = Color(0xFFEFF6FF), // Blue 50
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                            onStoreClick = { storeName, items ->
                                if (filterStoreName == storeName) {
                                    viewModel.clearStoreFilter()
                                } else {
                                    viewModel.setStoreFilter(storeName, items)
                                }
                            }
                        )
                    }
                }
                
                // Items
                items(filteredItems, key = { it.id }) { item ->
                    ShoppingListItem(
                        item = item,
                        isSelected = selectedItemIds.contains(item.id),
                        inSelectionMode = inSelectionMode,
                        onToggleDone = { viewModel.toggleItemDone(item.id) },
                        onToggleSelection = { viewModel.toggleSelection(item.id) },
                        onEdit = { 
                            if (inSelectionMode) {
                                // If in selection mode, long press just toggles selection again (or does nothing)
                                viewModel.toggleSelection(item.id)
                            } else {
                                // Enter selection mode on long press
                                viewModel.toggleSelection(item.id)
                            }
                        },
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 2.dp)
                    )
                }

                if (filteredItems.isEmpty()) {
                    item {
                        com.example.omiri.ui.components.OmiriEmptyState(
                            icon = Icons.Outlined.ShoppingCart,
                            title = "Your list is empty",
                            message = "Add some items to get started",
                            buttonText = "Add Item",
                            onButtonClick = { showAddItemDialog = true },
                            modifier = Modifier.padding(vertical = Spacing.xxl)
                        )
                    }
                }
            }
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp) // Above FAB/Nav
        ) { data ->
            Surface(
                color = Color(0xFF1F2937), // Dark Gray
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Green Check
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, // Need Check icon
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Floating action button
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
                .padding(bottom = 110.dp), // Above Bottom Nav (96dp) + Margin
            containerColor = Color(0xFFFE8357),
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
            onDismiss = { 
                showAddItemDialog = false 
                itemToEdit = null 
            },
            onAdd = { itemName, categoryId, isRecurring ->
                val currentItem = itemToEdit
                if (currentItem != null) {
                    viewModel.updateItem(currentItem.id, itemName, categoryId, isRecurring)
                } else {
                    viewModel.addItem(itemName, categoryId, isInDeals = false, isRecurring = isRecurring)
                    // Notification
                    scope.launch {
                        snackbarHostState.showSnackbar("Item added to list successfully")
                    }
                }
                showAddItemDialog = false
                itemToEdit = null
            },
            initialItem = itemToEdit
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
            onDeleteList = { viewModel.deleteList(it) },
            onResetList = { viewModel.resetRecurringItems() },
            onDismiss = { showListSelectionSheet = false }
        )
    }

    if (showMoveSelectionSheet) {
        // Reuse same sheet but for picking target
        com.example.omiri.ui.components.ListSelectionBottomSheet(
            shoppingLists = shoppingLists.filter { it.id != currentListId }, // Exclude current list
            currentListId = null, 
            onListSelected = { targetListId ->
                val count = selectedItemIds.size
                viewModel.moveSelectedItems(targetListId)
                scope.launch {
                    snackbarHostState.showSnackbar("Moved $count items")
                }
                showMoveSelectionSheet = false
            },
            onCreateListClick = {
                showMoveSelectionSheet = false
                showCreateListDialog = true 
            },
            onDeleteList = { /* Disable delete in move mode */ },
            onResetList = { /* Disable reset */ },
            onDismiss = { showMoveSelectionSheet = false }
        )
    }
}

@Composable
fun FilterChipStub(text: String, selected: Boolean, color: Color = Color.White) {
    Surface(
        color = if (selected) color else Color.White,
        shape = RoundedCornerShape(20.dp), // Pill shape
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null, // Gray border if not selected
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF374151)
            )
        }
    }
}
