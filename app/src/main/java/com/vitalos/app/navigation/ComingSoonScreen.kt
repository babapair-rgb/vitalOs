package com.vitalos.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ComingSoonScreen(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VitalOsTheme.colors.voidBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = VitalOsTheme.typography.headlineMd, color = VitalOsTheme.colors.onSurface)
        Text("Coming in a later phase", style = VitalOsTheme.typography.bodyMd, color = VitalOsTheme.colors.textSecondary)
    }
}
