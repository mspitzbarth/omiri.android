package com.example.omiri.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.omiri.ui.navigation.Routes

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int? = null
)

@Composable
fun PennyBottomNav(
    navController: NavController,
    viewModel: com.example.omiri.viewmodels.ShoppingListViewModel,
    modifier: Modifier = Modifier
) {
    val shoppingListCount = viewModel.totalUnfinishedItemsCount.collectAsState(initial = 0).value

    val items = listOf(
        BottomNavItem(
            route = Routes.Home,
            label = "Home",
            icon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = Routes.AllDeals,
            label = "Deals",
            icon = Icons.Outlined.LocalOffer
        ),
        BottomNavItem(
            route = Routes.AiChat,
            label = "AI",
            icon = Icons.Outlined.AutoAwesome
        ),
        BottomNavItem(
            route = Routes.ShoppingList,
            label = "List",
            icon = Icons.Outlined.ShoppingCart,
            badgeCount = shoppingListCount
        ),
        BottomNavItem(
            route = Routes.Settings,
            label = "Settings",
            icon = Icons.Outlined.Settings
        )
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    val bg = Color.White
    val topBorder = Color(0xFFE5E7EB)
    val active = Color(0xFFEA580B)
    val inactive = Color(0xFF6B7280)

    Column(modifier = modifier) {
        HorizontalDivider(
            color = topBorder,
            thickness = 1.dp
        )

        NavigationBar(
            modifier = Modifier.height(106.dp),
            containerColor = bg,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                // Hierarchical Selection Logic
                val selected = when (item.route) {
                    Routes.Settings -> currentRoute == Routes.Settings || 
                                     currentRoute == Routes.MyStores || 
                                     currentRoute == Routes.MembershipCards
                    Routes.ShoppingList -> currentRoute == Routes.ShoppingList || 
                                          currentRoute == Routes.ShoppingListMatches
                    Routes.Home -> currentRoute == Routes.Home
                    else -> currentRoute == item.route
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        // Always reset to the route's start destination (parent)
                        if (currentRoute != item.route) {
                             navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true // Save state of the destination we are LEAVING? Or just pop?
                                    // User said: "always go back to the parent"
                                    // Usually this means: Don't restore the stack of the target.
                                }
                                launchSingleTop = true
                                restoreState = false // Reset target stack
                            }
                        } else {
                            // Already on the root of this tab?
                            // If we are deep in stack for this tab (e.g. MyStores -> Settings is current tab route but actual dest is MyStores),
                            // currentRoute check in this file is simple string comparison.
                            // However, `currentRoute` variable earlier is: `navController.currentBackStackEntryAsState().value?.destination?.route`
                            // If we are deep, `currentRoute` might be "my_stores", but `item.route` is "settings".
                            // So `currentRoute != item.route` will be true.
                            // So we navigate to "settings" with restoreState=false.
                            
                            // If we are AT "settings" root. currentRoute == "settings".
                            // Then we do nothing.
                        }
                    },
                    icon = {
                        val count = item.badgeCount ?: 0
                        if (count > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = active,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 10.sp,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        }
                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = active,
                        unselectedIconColor = inactive,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
