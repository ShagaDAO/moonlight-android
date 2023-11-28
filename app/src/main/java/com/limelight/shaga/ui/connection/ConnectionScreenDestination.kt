package com.limelight.shaga.ui.connection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.limelight.shaga.ui.ScreenDestination

object ConnectionScreenDestination : ScreenDestination() {
    private const val baseRoute = "connection"
    private const val clientAccountArg = "clientAccount"
    private const val ipAddressArg = "ipAddress"
    private const val authorityArg = "authority"

    override val route: String = "$baseRoute/{$clientAccountArg}/{$ipAddressArg}/{$authorityArg}"

    override val arguments: List<NamedNavArgument>
        get() = listOf(
            navArgument(clientAccountArg) { type = NavType.StringType },
            navArgument(ipAddressArg) { type = NavType.StringType },
            navArgument(authorityArg) { type = NavType.StringType }
        )

    fun navigationRoute(clientAccount: String, ipAddress: String, authority: String): String {
        return "$baseRoute/$clientAccount/$ipAddress/$authority"
    }

    @Composable
    override fun Content(navBackStackEntry: NavBackStackEntry) {
        val clientAccount = navBackStackEntry.arguments?.getString(clientAccountArg)!!
        val ipAddress = navBackStackEntry.arguments?.getString(ipAddressArg)!!
        val authority = navBackStackEntry.arguments?.getString(authorityArg)!!

        val viewModel = viewModel { ConnectionScreenViewModel() }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ConnectionScreen(uiState, onRetryClick = {})
    }
}