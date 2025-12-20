package com.example.omiri.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.omiri.ui.components.BottomNav
import com.example.omiri.ui.screens.*
import com.example.omiri.viewmodels.ChatViewModel

@Composable
fun AppNavGraph(
    startDestination: String = Routes.Home,
    settingsViewModel: com.example.omiri.viewmodels.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Shared ViewModel
    // Shared ViewModel
    val productViewModel: com.example.omiri.viewmodels.ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val shoppingListViewModel: com.example.omiri.viewmodels.ShoppingListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val membershipCardViewModel: com.example.omiri.viewmodels.MembershipCardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val chatViewModel: com.example.omiri.viewmodels.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    // Handle Notification Deep Links
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val intent = activity?.intent
        if (intent?.getStringExtra("NAVIGATE_TO") == "shopping_list_matches") {
            navController.navigate(Routes.ShoppingListMatches)
            intent.removeExtra("NAVIGATE_TO")
        }
    }

    // Observe Selection Mode
    val inSelectionMode by shoppingListViewModel.inSelectionMode.collectAsState()

    Scaffold(
        topBar = {
            val isMainScreen = currentRoute in setOf(Routes.Home, Routes.AllDeals, Routes.ShoppingList, Routes.AiChat) || currentRoute?.startsWith(Routes.AllDealsBase) == true
            
            // Logic to determine if we show the global header
            // 1. Must be a main screen (or All Deals variants)
            // 2. Must NOT be in selection mode (Shopping List)
            // 3. Must NOT be in search mode? (If search bar is separate, header stays. If search replaces header, hide it. derived from VM?)
            //    For now, assume Header always visible unless selection.
            
            if (isMainScreen && !inSelectionMode) {
                val customAction: (@Composable () -> Unit)? = null // Explicitly declare it back
                val dropdownContent: (@Composable ColumnScope.(onDismiss: () -> Unit) -> Unit)? = 
                    if (currentRoute == Routes.AiChat) {
                        { onDismiss ->
                            DropdownMenuItem(
                                text = { Text("Delete chat") },
                                onClick = { 
                                    chatViewModel.resetConversation()
                                    onDismiss()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    } else null

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    com.example.omiri.ui.components.OmiriHeader(
                        notificationCount = 2, // Todo: Observe from VM
                        onNotificationClick = { navController.navigate(Routes.Notifications) },
                        onProfileClick = { navController.navigate(Routes.Settings) },
                        customAction = customAction,
                        dropdownContent = dropdownContent,
                        modifier = Modifier.statusBarsPadding()
                    )
                }
            }
        },
        bottomBar = {
            val showBottomNav = currentRoute in setOf(Routes.Home, Routes.Recipes, Routes.AiChat, Routes.ShoppingList) || 
                                currentRoute?.startsWith(Routes.AllDealsBase) == true
            if (showBottomNav) {
                BottomNav(
                    navController = navController,
                    viewModel = shoppingListViewModel
                )
            }
        }
    ) { padding ->
        val navHostPadding = if (currentRoute == "onboarding") androidx.compose.foundation.layout.PaddingValues(0.dp) else padding
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(navHostPadding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onNavigateAllDeals = { navController.navigate(Routes.AllDeals) },
                    onDealClick = { dealId: String -> navController.navigate(Routes.productDetails(dealId)) },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onProfileClick = { navController.navigate(Routes.Settings) },
                    onToggleShoppingList = { deal, isListed -> shoppingListViewModel.setDealListed(deal, isListed) },
                    onNavigateToShoppingListTab = { navController.navigate(Routes.ShoppingList) },
                    onNavigateToList = { listId ->
                        shoppingListViewModel.switchList(listId)
                        navController.navigate(Routes.ShoppingList)

                    },
                    shoppingListViewModel = shoppingListViewModel,
                    viewModel = productViewModel
                )
            }
            composable(
                route = Routes.AllDeals,
                arguments = listOf(
                    navArgument(Routes.AllDealsArgQuery) { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument(Routes.AllDealsArgFilter) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val initialQuery = backStackEntry.arguments?.getString(Routes.AllDealsArgQuery)
                val initialFilter = backStackEntry.arguments?.getString(Routes.AllDealsArgFilter)

                AllDealsScreen(
                    initialQuery = initialQuery,
                    initialFilterMode = initialFilter,
                    onDealClick = { dealId: String -> navController.navigate(Routes.productDetails(dealId)) },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onProfileClick = { navController.navigate(Routes.Settings) },
                    onToggleShoppingList = { deal, isListed -> 
                        shoppingListViewModel.setDealListed(deal, isListed)
                        productViewModel.toggleShoppingList(deal)
                    },
                    onNavigateToMyStores = { navController.navigate(Routes.MyStores) },
                    settingsViewModel = settingsViewModel,
                    viewModel = productViewModel
                )
            }
            composable(Routes.Recipes) {
                RecipesScreen(
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onProfileClick = { navController.navigate(Routes.Settings) }
                )
            }
            composable(Routes.AiChat) {
                AiChatScreen(
                    onBackClick = { navController.navigateUp() },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onProfileClick = { navController.navigate(Routes.Settings) },
                    onNavigateToShoppingList = { 
                        navController.navigate(Routes.ShoppingList) {
                            launchSingleTop = true
                        }
                    },
                    viewModel = chatViewModel
                )
            }
            composable(Routes.ShoppingList) {
                ShoppingListScreen(
                    viewModel = shoppingListViewModel,
                    productViewModel = productViewModel,
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onProfileClick = { navController.navigate(Routes.Settings) },
                    onProductClick = { dealId -> navController.navigate(Routes.productDetails(dealId)) },
                    onSearchDeals = { query ->
                        navController.navigate(Routes.allDeals(query = query, filter = "MY_DEALS"))
                    }
                )
            }
            composable(Routes.Settings) {
                com.example.omiri.ui.screens.SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onMyStoresClick = { navController.navigate("my_stores") },
                onMembershipCardsClick = { navController.navigate("membership_cards") },
                onInterestsClick = { navController.navigate(Routes.Interests) },
                onOnboardingClick = { navController.navigate("onboarding") },
                onComponentGalleryClick = { navController.navigate(Routes.ComponentGallery) },
                viewModel = settingsViewModel
            )
            }
            composable(Routes.ComponentGallery) {
                ComponentGalleryScreen()
            }
            composable(Routes.Interests) {
                InterestsScreen(
                    onBackClick = { navController.navigateUp() },
                    viewModel = settingsViewModel
                )
            }
            composable(Routes.MyStores) {
                MyStoresScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable(
                route = Routes.ProductDetails,
                arguments = listOf(navArgument(Routes.ProductDetailsArg) { type = NavType.StringType })
            ) { backStackEntry ->
                val dealId = backStackEntry.arguments?.getString(Routes.ProductDetailsArg)
                ProductDetailsScreen(
                    dealId = dealId,
                    onBackClick = { navController.navigateUp() },
                    onAddToList = { deal, isListed -> 
                         shoppingListViewModel.setDealListed(deal, isListed)
                         productViewModel.toggleShoppingList(deal)
                    },
                    onViewFlyer = { url, store, page ->
                        navController.navigate(Routes.flyerViewer(url, store, page))
                    },
                    viewModel = productViewModel
                )
            }
            
            composable(
                route = Routes.WebView,
                arguments = listOf(
                    navArgument(Routes.WebViewArgUrl) { type = NavType.StringType },
                    navArgument(Routes.WebViewArgTitle) { type = NavType.StringType; defaultValue = "Web View" }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString(Routes.WebViewArgUrl) ?: ""
                val title = backStackEntry.arguments?.getString(Routes.WebViewArgTitle) ?: "Web View"
                WebViewScreen(
                    url = url,
                    title = title,
                    onBackClick = { navController.navigateUp() }
                )
            }
            
            composable(
                route = Routes.FlyerViewer,
                arguments = listOf(
                    navArgument(Routes.FlyerArgUrl) { type = NavType.StringType },
                    navArgument(Routes.FlyerArgStore) { type = NavType.StringType },
                    navArgument(Routes.FlyerArgPage) { type = NavType.IntType; defaultValue = -1 } // Default -1 meant unused/0
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString(Routes.FlyerArgUrl) ?: ""
                val store = backStackEntry.arguments?.getString(Routes.FlyerArgStore) ?: "Store"
                val page = backStackEntry.arguments?.getInt(Routes.FlyerArgPage)?.takeIf { it >= 0 }
                
                FlyerViewerScreen(
                    pdfUrl = url,
                    storeName = store,
                    initialPage = page ?: 0,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(Routes.Notifications) {
                NotificationsScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable(Routes.ShoppingListMatches) {
                ShoppingListMatchesScreen(
                    onBackClick = { navController.navigateUp() },
                    onDealClick = { dealId ->
                        navController.navigate(Routes.productDetails(dealId))
                    }
                )
            }
            composable(Routes.MembershipCards) {
            MembershipCardsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = membershipCardViewModel
            )
        }
        composable("onboarding") {
            com.example.omiri.ui.screens.OnboardingScreen(
                onFinish = { 
                    settingsViewModel.completeOnboarding()
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack() 
                    } else {
                        // Was start destination
                        navController.navigate(Routes.Home) {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
}
