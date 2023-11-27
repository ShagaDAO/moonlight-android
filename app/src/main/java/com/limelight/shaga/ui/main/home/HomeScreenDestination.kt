package com.limelight.shaga.ui.main.home

import androidx.compose.runtime.Composable
import com.limelight.shaga.ui.ScreenDestination

object HomeScreenDestination : ScreenDestination(){
    override val route: String = "home"

    @Composable
    override fun Content() {
        HomeScreen()
    }
}