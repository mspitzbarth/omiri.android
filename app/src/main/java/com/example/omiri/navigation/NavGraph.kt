package com.example.omiri.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.omiri.screens.AllDealsScreen

sealed class Screen(val route: String) {
    object AllDeals : Screen("all_deals")
    // Add more screens here as needed
    // object Profile : Screen("profile")
    // object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.AllDeals.route
    ) {
        composable(route = Screen.AllDeals.route) {
            AllDealsScreen(navController = navController)
        }

        // Add more screens here
        // composable(route = Screen.Profile.route) {
        //     ProfileScreen(navController = navController)
        // }
    }
}
