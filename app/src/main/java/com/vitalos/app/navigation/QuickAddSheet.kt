package com.vitalos.app.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vitalos.app.designsystem.VitalOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onLogWater: () -> Unit,
    onStartYoga: () -> Unit,
    onRecordMeal: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = VitalOsTheme.colors.elevatedSurface) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = VitalOsTheme.spacing.lg)) {
            Text(
                "QUICK ADD",
                style = VitalOsTheme.typography.labelCaps,
                color = VitalOsTheme.colors.textSecondary,
                modifier = Modifier.padding(horizontal = VitalOsTheme.spacing.md),
            )
            ListItem(
                headlineContent = { Text("Log Water", color = VitalOsTheme.colors.onSurface) },
                leadingContent = { Icon(Icons.Outlined.LocalDrink, contentDescription = null, tint = VitalOsTheme.colors.diagnosticBlue) },
                modifier = Modifier.clickableRow(onLogWater),
            )
            ListItem(
                headlineContent = { Text("Start Yoga", color = VitalOsTheme.colors.onSurface) },
                leadingContent = { Icon(Icons.Outlined.SelfImprovement, contentDescription = null, tint = VitalOsTheme.colors.recoveryMint) },
                modifier = Modifier.clickableRow(onStartYoga),
            )
            ListItem(
                headlineContent = { Text("Record Meal", color = VitalOsTheme.colors.onSurface) },
                supportingContent = { Text("Coming in Phase 2", color = VitalOsTheme.colors.textSecondary) },
                leadingContent = { Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = VitalOsTheme.colors.readinessLime) },
                modifier = Modifier.clickableRow(onRecordMeal),
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
