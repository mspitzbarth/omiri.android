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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.*

import com.example.omiri.ui.components.*
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

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {}
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
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                bottom = 100.dp, // Space for FAB
                top = 0.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(listState)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Header (List Selector)
                    ShoppingListHeader(
                        listName = currentList?.name ?: "My List",
                        itemCount = totalItemsCount,
                        matchedDealsCount = matchedDealsCount,
                        onClick = { showListSelectionSheet = true },
                        isCheckingDeals = isCheckingDeals
                    )

                    // 2. Sort/Filter/View Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OmiriButton(
                                text = when(currentSortOption) {
                                    com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.STORE -> "Sort: Store"
                                    com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CATEGORY -> "Sort: Category"
                                    com.example.omiri.viewmodels.ShoppingListViewModel.SortOption.CUSTOM -> "Sort: Custom"
                                },
                                onClick = { showSortMenu = true },
                                style = OmiriButtonStyle.Neutral,
                                size = OmiriButtonSize.Small,
                                leadingIcon = Icons.Default.Sort,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
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
                        
                        OmiriButton(
                            text = "Filter",
                            onClick = { /* Filter */ },
                            style = OmiriButtonStyle.Neutral,
                            size = OmiriButtonSize.Small,
                            leadingIcon = Icons.Default.FilterList,
                            modifier = Modifier.weight(1f)
                        )
                        OmiriIconButton(
                            icon = Icons.Default.GridView,
                            onClick = { /* Toggle View */ },
                            style = OmiriButtonStyle.Neutral,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // 3. Deal Matched Alert
                    if (matchedDealsCount > 0) {
                        Surface(
                            color = Color(0xFFE8F7EF),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E9E5A).copy(alpha = 0.1f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E9E5A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$matchedDealsCount Deal${if (matchedDealsCount > 1) "s" else ""} Matched!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Neutral900
                                    )
                                    Text(
                                        text = "Save €${String.format("%.2f", savedAmount)} on this list",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.SubtleText
                                    )
                                }
                                OmiriButton(
                                    text = "View",
                                    onClick = { /* View deals */ },
                                    style = OmiriButtonStyle.Gradient, // Or a custom green style
                                    size = OmiriButtonSize.Small,
                                    fullWidth = false,
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
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
                                            .then(Modifier.draggableHandle())
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
                                            viewModel.toggleSelection(item.id)
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
                    )
                }

                // 6. Summary Section
                item {
                    com.example.omiri.ui.components.ShoppingListSummarySection(
                        totalItems = totalItemsCount,
                        checkedItems = filteredItems.count { it.isDone },
                        estimatedTotal = filteredItems.sumOf { it.discountPrice ?: it.price ?: 0.0 },
                        potentialSavings = savedAmount,
                        modifier = Modifier.padding(top = 16.dp)
                    )
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
