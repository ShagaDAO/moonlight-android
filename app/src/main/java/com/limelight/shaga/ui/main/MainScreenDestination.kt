package com.limelight.shaga.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limelight.shaga.ui.ScreenDestination
import com.limelight.shagaProtocol.DecodedAffairsData

class MainScreenDestination(
    val onOpenPairing: (clientString: String, data: DecodedAffairsData) -> Unit
) : ScreenDestination() {
    override val route: String = "main"

    @Composable
    override fun Content() {
        val viewModel = viewModel<MainScreenViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        MainScreen(uiState, onOpenPairing)
    }
}