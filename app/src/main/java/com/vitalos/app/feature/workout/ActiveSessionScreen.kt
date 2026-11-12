package com.vitalos.app.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ActiveSessionScreen(
    onEnded: (Long) -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(VitalOsTheme.spacing.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("YOGA SESSION", style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.textSecondary)
        Text(
            "${state.elapsedSeconds / 60}:${(state.elapsedSeconds % 60).toString().padStart(2, '0')}",
            style = VitalOsTheme.typography.displayHeroMobile,
            color = VitalOsTheme.colors.readinessLime,
        )
        Text("${state.liveHr ?: "--"} BPM", style = VitalOsTheme.typography.dataTabularMd, color = VitalOsTheme.colors.criticalRed)

        Column(modifier = Modifier.padding(top = VitalOsTheme.spacing.lg)) {
            OutlinedButton(onClick = { viewModel.togglePause() }) {
                Text(if (state.isRunning) "Pause" else "Resume")
            }
            Button(
                onClick = { viewModel.endSession(onEnded) },
                modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
            ) {
                Text("End Session")
            }
        }
    }
}
