package com.vitalos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.vitalos.app.designsystem.VitalOsAppTheme
import com.vitalos.app.navigation.QuickAddSheet
import com.vitalos.app.navigation.QuickAddViewModel
import com.vitalos.app.navigation.VitalOsBottomBar
import com.vitalos.app.navigation.VitalOsNavHost
import com.vitalos.app.navigation.VitalOsRoutes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalOsAppTheme {
                VitalOsAppScaffold()
            }
        }
    }
}

@Composable
private fun VitalOsAppScaffold() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showQuickAdd by remember { mutableStateOf(false) }
    val quickAddViewModel: QuickAddViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            VitalOsBottomBar(navController = navController, onQuickAddClick = { showQuickAdd = true })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        VitalOsNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onLogWater = {
                quickAddViewModel.logWater {
                    showQuickAdd = false
                    scope.launch { snackbarHostState.showSnackbar("+250 mL added") }
                }
            },
            onStartYoga = {
                quickAddViewModel.startYogaSession { workoutId ->
                    showQuickAdd = false
                    navController.navigate(VitalOsRoutes.workoutActive(workoutId))
                }
            },
            onRecordMeal = {
                showQuickAdd = false
                scope.launch { snackbarHostState.showSnackbar("Meal logging coming in Phase 2") }
            },
        )
    }
}
