package com.vitalos.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.vitalos.app.components.VitalCard
import com.vitalos.app.designsystem.VitalOsTheme

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(VitalOsTheme.spacing.margin),
        verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.md),
    ) {
        item {
            VitalCard(modifier = Modifier.fillMaxWidth()) {
                Text(state.tierBadge, style = VitalOsTheme.typography.labelCaps, color = VitalOsTheme.colors.recoveryMint)
                Text("EDIT PROFILE", style = VitalOsTheme.typography.headlineSm, color = VitalOsTheme.colors.onSurface)

                Column(verticalArrangement = Arrangement.spacedBy(VitalOsTheme.spacing.sm), modifier = Modifier.padding(top = VitalOsTheme.spacing.md)) {
                    ProfileField(label = "Name", value = state.name, onValueChange = viewModel::onNameChanged)
                    ProfileField(label = "Daily step goal", value = state.dailyStepGoal, onValueChange = viewModel::onStepGoalChanged, numeric = true)
                    ProfileField(label = "Daily hydration goal (mL)", value = state.dailyHydrationGoalMl, onValueChange = viewModel::onHydrationGoalChanged, numeric = true)
                    ProfileField(label = "Daily calorie goal", value = state.dailyCalorieGoal, onValueChange = viewModel::onCalorieGoalChanged, numeric = true)
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().padding(top = VitalOsTheme.spacing.md),
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save")
                }

                if (state.saveSuccessEvent > 0 && !state.isSaving) {
                    Text(
                        "Saved",
                        style = VitalOsTheme.typography.bodySm,
                        color = VitalOsTheme.colors.readinessLime,
                        modifier = Modifier.padding(top = VitalOsTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    numeric: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = VitalOsTheme.colors.onSurface,
            unfocusedTextColor = VitalOsTheme.colors.onSurface,
            focusedContainerColor = VitalOsTheme.colors.surfaceContainerLow,
            unfocusedContainerColor = VitalOsTheme.colors.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
