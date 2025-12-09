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
    
    // Smart Plan
    val smartPlan by (productViewModel?.smartPlan ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.example.omiri.data.models.ShoppingItem?>(null) }
    
    // Header Stats (Mocked or calculated)
    val totalItemsCount = filteredItems.size
    val matchedDealsCount = filteredItems.count { it.isInDeals || it.discountPrice != null }
    val savedAmount = 12.40 // Mocked per image, or calculate if data available

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)) // Ensure background is light gray so white items pop
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header (Notification/Profile)
            com.example.omiri.ui.components.OmiriHeader(
                notificationCount = 2,
                onNotificationClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )

            // Content List
            val listState = rememberLazyListState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .simpleVerticalScrollbar(listState),
                contentPadding = PaddingValues(bottom = Spacing.xxxl)
            ) {
                // Screen Header Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg)
                            .padding(top = Spacing.md, bottom = Spacing.md)
                    ) {
                        // Caption
                        Text(
                            text = "Active List",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280) // Gray 500
                        )
                        
                        // Title Row
                        var showListSelectionSheet by remember { mutableStateOf(false) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { showListSelectionSheet = true }
                        ) {
                            Text(
                                text = currentList?.name ?: "Weekly Groceries",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "Switch List",
                                tint = Color(0xFF6B7280)
                            )
                            Spacer(Modifier.weight(1f))
                            
                            // Edit Icon
                            IconButton(onClick = { /* Edit List Name? */ }) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Edit, "Edit", tint = Color(0xFF4B5563), modifier = Modifier.size(20.dp))
                                }
                            }
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

                        Spacer(Modifier.height(4.dp))
                        
                        // Stats Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalItemsCount items • $matchedDealsCount matched deals",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "You saved €${String.format("%.2f", savedAmount)} this week",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF16A34A), // Green
                                fontWeight = FontWeight.Bold
                            )
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
                        // All Chip (Active) - Changed to Blue as requested
                        FilterChipStub(text = "All ($totalItemsCount)", selected = true, color = Color(0xFF2563EB))
                        
                        // Mock Categories
                        FilterChipStub(text = "Produce (4)", selected = false)
                        FilterChipStub(text = "Dairy (3)", selected = false)
                        FilterChipStub(text = "Household (2)", selected = false)
                    }
                }
                
                // Recommended Store Run
                item {
                    if (smartPlan != null) {
                        RecommendedStoreRunCard(
                            plan = smartPlan!!,
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                        )
                    }
                }
                
                // Items
                items(filteredItems, key = { it.id }) { item ->
                    ShoppingListItem(
                        item = item,
                        onToggleDone = { viewModel.toggleItemDone(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onEdit = { 
                            itemToEdit = item
                            showAddItemDialog = true 
                        },
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = 4.dp)
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF374151)
            )
        }
    }
}
