package com.example.omiri.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem

import com.example.omiri.ui.components.RecommendedRouteCard
import com.example.omiri.ui.components.RouteStepUi
import com.example.omiri.ui.components.ShoppingListHeader
import com.example.omiri.ui.components.ShoppingListSummaryCard
import com.example.omiri.ui.components.AddItemBar
import com.example.omiri.ui.components.ShoppingListItem
import com.example.omiri.ui.components.simpleVerticalScrollbar
import com.example.omiri.ui.theme.Spacing
import com.example.omiri.ui.theme.AppColors
import com.example.omiri.viewmodels.ShoppingListViewModel
import com.example.omiri.viewmodels.ProductViewModel
import com.example.omiri.viewmodels.CategoryUiModel

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = viewModel(),
    productViewModel: ProductViewModel? = null,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onProductClick: (String) -> Unit = {},
    onSearchDeals: (String) -> Unit = {}
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val currentListId by viewModel.currentListId.collectAsState()
    val currentList by viewModel.currentList.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val groupedItems by viewModel.groupedItems.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val isCheckingDeals by viewModel.isCheckingDeals.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterStoreName by viewModel.filterStoreName.collectAsState()
    
    // Selection Mode State
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    val inSelectionMode by viewModel.inSelectionMode.collectAsState()
    
    // Smart Plan
    val smartPlan by viewModel.smartPlan.collectAsState()
    
    // Find Deals State
    val findingDealsFor by viewModel.findingDealsForItem.collectAsState()
    val dealSearchResults by viewModel.dealSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboard.current

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showMoveSelectionSheet by remember { mutableStateOf(false) }
    var showListSelectionSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.example.omiri.data.models.ShoppingItem?>(null) }
    var newItemText by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val currentSortOption by viewModel.currentSortOption.collectAsState()
    
    // Header Stats
    val totalItemsCount = currentList?.items?.size ?: 0
    val matchedDealsCount = filteredItems.count { it.isInDeals || it.discountPrice != null }
    val savedAmount = filteredItems.sumOf { 
        if (it.price != null && it.discountPrice != null && it.discountPrice < it.price) {
            it.price - it.discountPrice
        } else 0.0
    } // Dynamic Calculation

    // Nested Scroll Setup for Collapsible Header
    val density = androidx.compose.ui.platform.LocalDensity.current
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    
    val nestedScrollConnection = remember(headerHeightPx) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (inSelectionMode) return androidx.compose.ui.geometry.Offset.Zero
                
                val delta = available.y
                val newOffset = (headerOffsetPx + delta).coerceIn(-headerHeightPx, 0f)
                headerOffsetPx = newOffset
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.omiri.ui.theme.AppColors.Bg)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Header (Notification/Profile OR Contextual Selection Bar)
        if (inSelectionMode) {
             com.example.omiri.ui.components.ContextualSelectionTopBar(
                selectedCount = selectedItemIds.size,
                onClearSelection = { viewModel.clearSelection() },
                onDelete = { viewModel.deleteSelectedItems() },
                onDuplicate = { 
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
                    val id = selectedItemIds.firstOrNull()
                    if (id != null) {
                        val item = filteredItems.find { it.id == id }
                        if (item != null) {
                            itemToEdit = item
                            showAddItemDialog = true
                            viewModel.clearSelection()
                        }
                    }
                },
                onViewDeal = if (selectedItemIds.size == 1) {
                     {
                         val id = selectedItemIds.firstOrNull()
                         if (id != null) {
                             val item = filteredItems.find { it.id == id }
                             if (item != null) {
                                viewModel.clearSelection()
                                // Use same logic as ShoppingListItem
                                 onSearchDeals(item.name)
                             }
                         }
                     }
                } else null,
                hasAlternatives = if (selectedItemIds.size == 1) {
                    val id = selectedItemIds.firstOrNull()
                    val item = filteredItems.find { it.id == id }
                    (item?.alternativesCount ?: 0) > 0
                } else false,
                modifier = Modifier.align(Alignment.TopCenter).zIndex(2f)
            )
        }


        // Content List
        val listState = rememberLazyListState()
        
        // Reorderable State
        val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
            val fromId = from.key as? String
            val toId = to.key as? String
            if (fromId != null && toId != null) {
                viewModel.reorderItemById(fromId, toId)
            }
        }
        
        val headerHeightDp = with(density) { headerHeightPx.toDp() }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                bottom = 32.dp,
                top = if (inSelectionMode) 64.dp else 0.dp // Spacer used for header offset
            ),
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(listState)
        ) {
            // Spacer for Collapsible Header
            if (!inSelectionMode) {
                item {
                    Spacer(modifier = Modifier.height(headerHeightDp))
                }
            }       // 1. Summary Card
                item {
                    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
                        if (matchedDealsCount > 0 && savedAmount > 0) {
                            ShoppingListSummaryCard(
                                itemCount = totalItemsCount,
                                storeCount = smartPlan?.steps?.size ?: 1, 
                                matchedDealsCount = matchedDealsCount,
                                totalSavings = savedAmount,
                                bestTime = "",
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                        
                        // Recommended Route (Real Data)
                        val plan = smartPlan
                        val steps = plan?.steps?.mapIndexed { index, step ->
                            com.example.omiri.ui.components.RouteStepUi(
                                index = index + 1,
                                storeName = step.storeName,
                                itemCount = step.itemsCount,
                                savings = step.stepSavings,
                                items = step.items,
                                color = when(step.storeName) {
                                    "Target" -> Color(0xFFE53935)
                                    "Walmart" -> Color(0xFF1E88E5)
                                    "Costco" -> Color(0xFF8E24AA)
                                    "Lidl" -> Color(0xFF0050AA)
                                    "Aldi" -> Color(0xFF0A2761)
                                    else -> AppColors.Neutral500
                                }
                            )
                        } ?: emptyList()

                        if (steps.isNotEmpty()) {
                            com.example.omiri.ui.components.RecommendedRouteCard(
                                steps = steps,
                                onViewMapClick = { /* Map View */ }
                            )
                        }
                        
                        // Spacer(Modifier.height(8.dp)) // Already using top padding of items
                        
                        // Category Filters (Reverted to List, Reduced Gap)
                        LazyRow(
                             contentPadding = PaddingValues(horizontal = 0.dp), // Zero because parent has padding
                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                             modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp) // Adjusted padding
                        ) {
                             // All
                             item {
                                 Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selectedCategory == null) AppColors.BrandOrange else AppColors.Neutral100,
                                    border = if (selectedCategory != null) androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200) else null,
                                    modifier = Modifier.height(32.dp).clickable { viewModel.selectCategory(null) }
                                 ) {
                                     Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                         Text(
                                             text = "All",
                                             style = MaterialTheme.typography.labelSmall,
                                             fontWeight = FontWeight.Medium,
                                             color = if (selectedCategory == null) AppColors.Surface else AppColors.Neutral700
                                         )
                                     }
                                 }
                             }
                             
                             items(availableCategories) { cat ->
                                 val isSelected = selectedCategory == cat.id
                                 Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) AppColors.BrandOrange else AppColors.Neutral100,
                                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200) else null,
                                    modifier = Modifier.height(32.dp).clickable { viewModel.selectCategory(cat.id) }
                                 ) {
                                     Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                         Text(
                                             text = "${cat.name} (${cat.count})",
                                             style = MaterialTheme.typography.labelSmall,
                                             fontWeight = FontWeight.Medium,
                                             color = if (isSelected) AppColors.Surface else AppColors.Neutral700
                                         )
                                     }
                                 }
                             }
                        }

                        // Spacer(Modifier.height(16.dp)) // Removed/Reduced
                    }
                }
                
                // ... (Use separate edits for rest if needed, but I'm just swapping order here)



                


                // 4. Items List Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items ($totalItemsCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Neutral900
                        )
                        
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { showSortMenu = true }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when(currentSortOption) {
                                        com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.STORE -> "Sort: Store"
                                        com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CATEGORY -> "Sort: Category"
                                        com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CUSTOM -> "Sort: Custom"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.BrandOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppColors.BrandOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Custom Order (Drag & Drop)") },
                                    onClick = { 
                                        viewModel.setSortOption(com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CUSTOM)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Store") },
                                    onClick = { 
                                        viewModel.setSortOption(com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.STORE)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sort by Category") },
                                    onClick = { 
                                        viewModel.setSortOption(com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CATEGORY)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 5. Items (Grouped)
                groupedItems.forEach { (header, itemsInGroup) ->
                    // Show Header
                    // Show Header
                    if (header.isNotEmpty()) {
                        item {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.Neutral500,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = Spacing.lg)
                                    .padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                    }

                    items(itemsInGroup, key = { it.id }) { item ->
                        val isCustomSort = currentSortOption == com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CUSTOM
                        

                        if (isCustomSort) {
                           ReorderableItem(state = reorderableState, key = item.id) { isDragging ->
                                val elevation = if (isDragging) 8.dp else 0.dp
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Spacing.lg, vertical = 6.dp)
                                        .scale(if (isDragging) 1.02f else 1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // External Drag Handle
                                    Icon(
                                        imageVector = Icons.Default.DragIndicator,
                                        contentDescription = "Drag to reorder",
                                        tint = if (isDragging) AppColors.BrandOrange else AppColors.Neutral400,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 8.dp)
                                            .padding(end = 8.dp)
                                            // .draggableHandle() // Removed unresolved reference
                                    )
                                    
                                    // Observe loading state for this item
                                    val loadingItemIds by viewModel.loadingItemIds.collectAsState()
                                    
                                    ShoppingListItem(
                                        item = item,
                                        isSelected = selectedItemIds.contains(item.id),
                                        inSelectionMode = inSelectionMode,
                                        isLoading = loadingItemIds.contains(item.id),
                                        onToggleDone = { viewModel.toggleItemDone(item.id) },
                                        onToggleSelection = { viewModel.toggleSelection(item.id) },
                                        onEdit = { 
                                            if (inSelectionMode) {
                                                viewModel.toggleSelection(item.id)
                                            } else {
                                                viewModel.toggleSelection(item.id)
                                            }
                                        },
                                        onFindDeals = { 
                                            viewModel.findDealsAndApply(item)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .run {
                                                if (isDragging) {
                                                    this
                                                        .shadow(8.dp, RoundedCornerShape(12.dp))
                                                        .border(2.dp, AppColors.BrandOrange, RoundedCornerShape(12.dp))
                                                } else {
                                                    this
                                                }
                                            }
                                    )
                                }
                           }
                        } else {
                            ShoppingListItem(
                                item = item,
                                isSelected = selectedItemIds.contains(item.id),
                                inSelectionMode = inSelectionMode,
                                onToggleDone = { viewModel.toggleItemDone(item.id) },
                                onToggleSelection = { viewModel.toggleSelection(item.id) },
                                onEdit = { 
                                    if (inSelectionMode) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        viewModel.toggleSelection(item.id)
                                    }
                                },
                                        onFindDeals = { 
                                            val itemCount = currentList?.items?.size ?: 0
                                                onSearchDeals(item.name)
                                        },
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (filteredItems.isEmpty()) {
                    item {
                        com.example.omiri.ui.components.OmiriEmptyState(
                            icon = Icons.Outlined.ShoppingCart,
                            title = "Your list is empty",
                            message = "Add some items to get started",
                            buttonText = null, // Button is now in bar above
                            onButtonClick = { },
                            modifier = Modifier.padding(vertical = Spacing.xxl)
                        )
                    }
                }
                
                // Bottom Ad
                item(key = "shopping_list_ad_banner") {
                    com.example.omiri.ui.components.AdCard(
                        modifier = Modifier
                            .padding(vertical = Spacing.lg)
                            // Remove horizontal padding if you want edge-to-edge or add it if needed. 
                            // AdCard default wraps content in center.
                            // Let's keep it centered.
                    )
                }
            }

        // 0. Collapsible Header (Title Only) - MOVED AFTER LIST
        if (!inSelectionMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                         headerHeightPx = coordinates.size.height.toFloat()
                    }
                    .graphicsLayer {
                         translationY = headerOffsetPx
                    }
                    .background(AppColors.Bg)
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            ) {
                com.example.omiri.ui.components.ShoppingListHeader(
                    listName = currentList?.name ?: "My List",
                    onClick = { showListSelectionSheet = true },
                    isCheckingDeals = isCheckingDeals
                )
                HorizontalDivider(color = AppColors.Neutral200, thickness = 1.dp)
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
                color = AppColors.Neutral800, // Dark Gray
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
                            .background(AppColors.Green500, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check, // Need Check icon
                            contentDescription = null,
                            tint = AppColors.Surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        color = AppColors.Surface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Floating action button
        // Floating action button
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
                .padding(bottom = 16.dp)
                .size(64.dp), // Match MembershipCardsScreen size
            shape = CircleShape, // Match MembershipCardsScreen shape
            containerColor = AppColors.BrandOrange,
            contentColor = AppColors.Surface
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add item",
                modifier = Modifier.size(32.dp) // Match MembershipCardsScreen icon size
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
            currentListId = currentListId ?: "",
            onListSelected = { targetId ->
                viewModel.switchList(targetId)
                showListSelectionSheet = false
            },
            onCreateListClick = { 
                showListSelectionSheet = false
                showCreateListDialog = true
            },
            onDeleteList = { listId ->
                viewModel.deleteList(listId)
            }, 
            onResetList = {},
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
        color = if (selected) color else AppColors.Surface,
        shape = RoundedCornerShape(20.dp), // Pill shape
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, AppColors.Neutral200) else null, // Gray border if not selected
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (selected) AppColors.Surface else AppColors.Neutral700
            )
        }
    }
}
