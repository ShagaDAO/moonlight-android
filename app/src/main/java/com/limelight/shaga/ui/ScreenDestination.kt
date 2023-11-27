package com.limelight.shaga.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

abstract class ScreenDestination {
    abstract val route: String

    protected open val arguments: List<NamedNavArgument>
        get() = emptyList()

    fun register(builder: NavGraphBuilder) {
        builder.composable(route, arguments) {
            Content()
        }
    }

    @Composable
    abstract fun Content()
}