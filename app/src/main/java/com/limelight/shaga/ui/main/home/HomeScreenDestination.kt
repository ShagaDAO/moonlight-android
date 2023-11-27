package com.limelight.shaga.ui.main.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.limelight.shaga.ui.ScreenDestination

object HomeScreenDestination : ScreenDestination(){
    override val route: String = "home"

    @Composable
    override fun Content(navBackStackEntry: NavBackStackEntry) {
        HomeScreen()
    }
}