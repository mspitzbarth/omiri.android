package com.example.omiri.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.navigation.navArgument
import com.example.omiri.ui.components.PennyBottomNav
import com.example.omiri.ui.screens.*

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
            if (currentRoute != "onboarding") {
                PennyBottomNav(
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
                    onToggleShoppingList = { deal, isListed -> shoppingListViewModel.setDealListed(deal, isListed) },
                    onNavigateToShoppingListTab = { navController.navigate(Routes.ShoppingList) },
                    onNavigateToList = { listId ->
                        shoppingListViewModel.switchList(listId)
                        navController.navigate(Routes.ShoppingList)
                    },
                    shoppingListViewModel = shoppingListViewModel
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
                com.example.omiri.ui.screens.SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onMyStoresClick = { navController.navigate("my_stores") },
                onMembershipCardsClick = { navController.navigate("membership_cards") },
                onOnboardingClick = { navController.navigate("onboarding") },
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
