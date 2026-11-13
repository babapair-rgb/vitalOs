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
        composable(VitalOsRoutes.HOME) {
            com.vitalos.app.feature.home.HomeScreen(onOpenDeviceManagement = { navController.navigate(VitalOsRoutes.DEVICE_MANAGEMENT) })
        }
        composable(VitalOsRoutes.ACTIVITY) { com.vitalos.app.feature.activity.ActivityScreen() }
        composable(VitalOsRoutes.HEALTH) {
            com.vitalos.app.feature.heart.HeartHealthScreen(onOpenSleep = { navController.navigate(VitalOsRoutes.SLEEP) })
        }
        composable(VitalOsRoutes.SLEEP) { com.vitalos.app.feature.sleep.SleepScreen() }
        composable(VitalOsRoutes.INSIGHTS) { ComingSoonScreen("Insights") }
        composable(VitalOsRoutes.PROFILE) { com.vitalos.app.feature.profile.ProfileScreen() }
        composable(VitalOsRoutes.WORKOUT_HUB) {
            com.vitalos.app.feature.workout.WorkoutHubScreen(
                onStartYoga = { workoutId -> navController.navigate(VitalOsRoutes.workoutActive(workoutId)) },
            )
        }
        composable(
            VitalOsRoutes.WORKOUT_ACTIVE,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) {
            com.vitalos.app.feature.workout.ActiveSessionScreen(
                onEnded = { workoutId ->
                    navController.navigate(VitalOsRoutes.workoutSummary(workoutId)) {
                        popUpTo(VitalOsRoutes.WORKOUT_HUB)
                    }
                },
            )
        }
        composable(
            VitalOsRoutes.WORKOUT_SUMMARY,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType }),
        ) {
            com.vitalos.app.feature.workout.WorkoutSummaryScreen(
                onDone = { navController.popBackStack(VitalOsRoutes.HOME, inclusive = false) },
            )
        }
        composable(VitalOsRoutes.DEVICE_MANAGEMENT) { com.vitalos.app.feature.device.DeviceManagementScreen() }
    }
}
