package com.limelight.shaga.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.limelight.shaga.ui.ScreenDestination

class MainScreenDestination(
    val onOpenPairing: (clientString: String, ipAddress: String, authority: String) -> Unit
) : ScreenDestination() {
    override val route: String = "main"

    @Composable
    override fun Content(navBackStackEntry: NavBackStackEntry) {
        val viewModel = viewModel<MainScreenViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        MainScreen(uiState, onOpenPairing)
    }
}