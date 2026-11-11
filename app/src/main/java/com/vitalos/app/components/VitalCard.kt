package com.vitalos.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun VitalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = VitalOsTheme.colors.cardSurface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(VitalOsTheme.shapes.card)
            .background(backgroundColor)
            .padding(VitalOsTheme.spacing.md),
        content = content,
    )
}
