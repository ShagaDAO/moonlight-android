package com.limelight.shaga.ui.main.games

import androidx.compose.runtime.Composable
import com.limelight.shaga.ui.ScreenDestination

object GameListDestination : ScreenDestination() {
    override val route: String
        get() = "game_list"

    @Composable
    override fun Content() {
        GameListScreen()
    }
}