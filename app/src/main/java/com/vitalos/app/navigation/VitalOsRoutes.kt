package com.vitalos.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

object VitalOsRoutes {
    const val HOME = "home"
    const val ACTIVITY = "activity"
    const val HEALTH = "health"
    const val INSIGHTS = "insights"
    const val PROFILE = "profile"
    const val WORKOUT_HUB = "workout_hub"
    const val WORKOUT_ACTIVE = "workout_active/{workoutId}"
    const val WORKOUT_SUMMARY = "workout_summary/{workoutId}"
    const val DEVICE_MANAGEMENT = "device_management"

    fun workoutActive(workoutId: Long) = "workout_active/$workoutId"
    fun workoutSummary(workoutId: Long) = "workout_summary/$workoutId"
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(VitalOsRoutes.HOME, "Home", Icons.Outlined.Cottage)
    object Activity : BottomNavItem(VitalOsRoutes.ACTIVITY, "Activity", Icons.Outlined.LocalFireDepartment)
    object Health : BottomNavItem(VitalOsRoutes.HEALTH, "Health", Icons.Outlined.MonitorHeart)
    object Insights : BottomNavItem(VitalOsRoutes.INSIGHTS, "Insights", Icons.Outlined.AutoAwesome)
    object Profile : BottomNavItem(VitalOsRoutes.PROFILE, "Profile", Icons.Outlined.Tune)

    companion object {
        val items: List<BottomNavItem> = listOf(Home, Activity, Health, Insights, Profile)
    }
}
