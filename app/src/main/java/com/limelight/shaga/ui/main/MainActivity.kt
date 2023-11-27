package com.limelight.shaga.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.limelight.shaga.ui.connection.ConnectionScreenDestination
import com.limelight.shaga.ui.kit.ShagaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShagaTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val navController = rememberNavController()

        val mainScreenDestination = MainScreenDestination(
            onOpenPairing = { clientString: String, ipAddress, authority ->
                navController.navigate(
                    ConnectionScreenDestination.navigationRoute(clientString, ipAddress, authority)
                )
            }
        )

        NavHost(navController = navController, startDestination = mainScreenDestination.route) {
            mainScreenDestination.register(this)
            ConnectionScreenDestination.register(this)
        }
    }

}