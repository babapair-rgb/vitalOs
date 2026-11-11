package com.vitalos.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun VitalOsNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = VitalOsRoutes.HOME, modifier = modifier) {
        composable(VitalOsRoutes.HOME) { com.vitalos.app.feature.home.HomeScreen() }
        composable(VitalOsRoutes.ACTIVITY) { ComingSoonScreen("Activity") }
        composable(VitalOsRoutes.HEALTH) { ComingSoonScreen("Health") }
        composable(VitalOsRoutes.INSIGHTS) { ComingSoonScreen("Insights") }
        composable(VitalOsRoutes.PROFILE) { ComingSoonScreen("Profile") }
        composable(VitalOsRoutes.WORKOUT_HUB) { ComingSoonScreen("Workout Hub") }
        composable(
            VitalOsRoutes.WORKOUT_ACTIVE,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Active Session") }
        composable(
            VitalOsRoutes.WORKOUT_SUMMARY,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) { ComingSoonScreen("Workout Summary") }
        composable(VitalOsRoutes.DEVICE_MANAGEMENT) { ComingSoonScreen("TAGG Verve") }
    }
}
