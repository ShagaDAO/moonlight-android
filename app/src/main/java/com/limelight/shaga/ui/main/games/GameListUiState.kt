package com.limelight.shaga.ui.main.games

import com.limelight.AppView

data class GameListUiState(
    val items: List<AppView.AppObject>,
    val ipAddress: String
)