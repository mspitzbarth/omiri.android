package com.example.omiri.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.RestaurantMenu
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


import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeCount: Int? = null,
    val isBeta: Boolean = false
)

@Composable
fun BottomNav(
    navController: NavController,
    viewModel: com.example.omiri.viewmodels.ShoppingListViewModel,
    modifier: Modifier = Modifier
) {
    val shoppingListCount = viewModel.totalUnfinishedItemsCount.collectAsState(initial = 0).value

    val items = listOf(
        BottomNavItem(
            route = Routes.Home,
            label = "Home",
            icon = Icons.Filled.Home
        ),
        BottomNavItem(
            route = Routes.AllDealsBase,
            label = "Deals",
            icon = Icons.Filled.LocalOffer
        ),
        BottomNavItem(
            route = Routes.ShoppingList,
            label = "List",
            icon = Icons.Filled.ShoppingCart,
            badgeCount = shoppingListCount
        ),
        BottomNavItem(
            route = Routes.AiChat,
            label = "AI",
            icon = Icons.Filled.AutoAwesome,
            isBeta = true
        ),
        BottomNavItem(
            route = Routes.Recipes,
            label = "Recipes",
            icon = Icons.Filled.RestaurantMenu,
            isBeta = true
        )
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    val bg = Color.White
    val topBorder = Color(0xFFE5E7EB)
    val active = Color(0xFFFE8357)
    val inactive = Color(0xFF6B7280)

    Column(modifier = modifier) {
        HorizontalDivider(
            color = topBorder,
            thickness = 1.dp
        )

        NavigationBar(
            modifier = Modifier.height(96.dp), // Increased height for more padding
            containerColor = bg,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                // Hierarchical Selection Logic
                val selected = when (item.route) {
                    Routes.Settings -> currentRoute == Routes.Settings // Settings removed from nav but logic might remain for safety? No, item removed.
                    Routes.ShoppingList -> currentRoute == Routes.ShoppingList || 
                                          currentRoute == Routes.ShoppingListMatches
                    Routes.Home -> currentRoute == Routes.Home
                    Routes.AllDealsBase -> currentRoute?.startsWith(Routes.AllDealsBase) == true
                    else -> currentRoute == item.route
                }

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                         navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        val count = item.badgeCount ?: 0
                        if (count > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = active,
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 9.sp,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else if (item.isBeta) {
                           BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFF3B82F6), // Blue
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = (10).dp, y = (-6).dp)
                                    ) {
                                        Text(
                                            text = "BETA",
                                            fontSize = 6.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 1.dp)
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = active,
                        unselectedIconColor = inactive,
                        indicatorColor = Color.Transparent,
                        selectedTextColor = active,
                        unselectedTextColor = inactive
                    )
                )
            }
        }
    }
}
