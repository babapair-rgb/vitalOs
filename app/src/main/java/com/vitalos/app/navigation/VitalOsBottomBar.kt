package com.vitalos.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun VitalOsBottomBar(
    navController: NavHostController,
    onQuickAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VitalOsTheme.colors.surfaceContainerLowest)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val leftItems = BottomNavItem.items.take(2)
            val rightItems = BottomNavItem.items.takeLast(3)

            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leftItems.forEach { item ->
                    BottomNavTab(item, currentRoute?.hierarchy?.any { it.route == item.route } == true) {
                        navController.navigateToTab(item.route)
                    }
                }
            }
            // Fixed-width gap exactly centered on the full-width Row (both side Rows share
            // weight(1f)), so it always lines up with the FAB below regardless of each tab's
            // own label width.
            Box(modifier = Modifier.width(56.dp).fillMaxHeight())
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rightItems.forEach { item ->
                    BottomNavTab(item, currentRoute?.hierarchy?.any { it.route == item.route } == true) {
                        navController.navigateToTab(item.route)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(VitalOsTheme.colors.readinessLime)
                .clickable(onClick = onQuickAddClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Quick add", tint = VitalOsTheme.colors.onPrimaryContainer)
        }
    }
}

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomNavTab(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textSecondary,
        )
        Text(
            item.label.uppercase(),
            style = VitalOsTheme.typography.labelCaps,
            color = if (selected) VitalOsTheme.colors.readinessLime else VitalOsTheme.colors.textSecondary,
        )
    }
}
