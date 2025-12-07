package com.example.omiri.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.omiri.ui.components.PennyBottomNav
import com.example.omiri.ui.screens.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    // Shared ViewModel
    // Shared ViewModel
    val shoppingListViewModel: com.example.omiri.viewmodels.ShoppingListViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val membershipCardViewModel: com.example.omiri.viewmodels.MembershipCardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
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

    Scaffold(
        bottomBar = {
            PennyBottomNav(
                navController = navController,
                viewModel = shoppingListViewModel
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onNavigateAllDeals = { navController.navigate(Routes.AllDeals) },
                    onDealClick = { dealId: String -> navController.navigate(Routes.productDetails(dealId)) },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onToggleShoppingList = { deal, isListed -> shoppingListViewModel.setDealListed(deal, isListed) }
                )
            }
            composable(Routes.AllDeals) {
                AllDealsScreen(
                    onDealClick = { dealId: String -> navController.navigate(Routes.productDetails(dealId)) },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    onToggleShoppingList = { deal, isListed -> shoppingListViewModel.setDealListed(deal, isListed) }
                )
            }
            composable(Routes.AiChat) {
            AiChatScreen(
                onNotificationsClick = { navController.navigate(Routes.Notifications) },
                onNavigateToShoppingList = { 
                    navController.navigate(Routes.ShoppingList) {
                        launchSingleTop = true
                    }
                }
            )
        }
            composable(Routes.ShoppingList) {
                // Assuming ShoppingListScreen can accept the viewModel or uses internal mechanism.
                // For better architecture, let's look at ShoppingListScreen signature or just pass it if we can edit it.
                // For now, assume we might need to refactor ShoppingListScreen to accept it, 
                // but since the original didn't take it, it probably creates its own. 
                // To SHARE state, we MUST pass this instance.
                ShoppingListScreen(
                    viewModel = shoppingListViewModel,
                    onNotificationsClick = { navController.navigate(Routes.Notifications) }
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    onBackClick = { navController.navigateUp() },
                    onMyStoresClick = { navController.navigate(Routes.MyStores) },
                    onMembershipCardsClick = { navController.navigate(Routes.MembershipCards) }
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
        }
    }
}
