package com.limelight.shaga.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.limelight.R
import com.limelight.shaga.ui.kit.ProfileButton
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTheme
import com.limelight.shaga.ui.kit.ShagaTopBar
import com.limelight.shaga.ui.main.games.GameListDestination
import com.limelight.shaga.ui.main.home.HomeScreenDestination
import com.limelight.shaga.ui.pairing.PairingStringDialog
import com.limelight.shaga.ui.pairing.session.SessionSettingsDialog
import com.limelight.shaga.ui.pairing.session.SessionSettingsViewModel
import com.limelight.shagaProtocol.PairingActivity

@Composable
fun MainScreen(
    state: MainScreenState,
    onOpenPairing: (clientString: String, ipAddress: String, authority: String) -> Unit = { _, _, _ -> }
) {
    var isPairingDialogVisible by remember { mutableStateOf(false) }
    var pairingString by remember { mutableStateOf<String?>(null) }
    var sessionSettingsDialogKey by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopBar(
                hasActiveRental = state.activeRental != null,
                onPairingStartClick = {
                    val rental = state.activeRental
                    if (rental != null) {
                        PairingActivity.start(
                            context,
                            rental.clientString,
                            rental.affairsData.ipAddress,
                            rental.affairsData.authority.toString()
                        )
                    } else {
                        isPairingDialogVisible = true
                    }
                },
                Modifier.fillMaxWidth()
            )
        },
        bottomBar = { BottomNavigation() },
        containerColor = ShagaColors.Background,
        contentColor = Color.White
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Image(
                painterResource(id = R.drawable.content_background),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            val navController = rememberNavController()
            NavHost(navController, startDestination = HomeScreenDestination.route) {
                HomeScreenDestination.register(this)
                GameListDestination.register(this)
            }
        }
    }

    if (isPairingDialogVisible) {
        PairingStringDialog(
            onConfirmClick = {
                sessionSettingsDialogKey++
                pairingString = it
            },
            onDismissRequest = { isPairingDialogVisible = false }
        )
    }

    if (pairingString != null) {
        SessionSettingsDialog(
            viewModel = viewModel(key = sessionSettingsDialogKey.toString()) {
                SessionSettingsViewModel(pairingString!!)
            },
            onDismissRequest = { pairingString = null },
            onOpenPairing = onOpenPairing
        )
    }
}

@Composable
private fun BottomNavigation(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier.height(52.dp),
        containerColor = ShagaColors.Background
    ) {
        MainNavigationItem.values().forEach { item ->
            NavigationBarItem(
                selected = item == MainNavigationItem.HOME,
                onClick = {},
                icon = {
                    Icon(
                        painterResource(item.icon),
                        contentDescription = item.name
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ShagaColors.Accent,
                    indicatorColor = ShagaColors.Background,
                    unselectedIconColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun TopBar(
    hasActiveRental: Boolean?,
    onPairingStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShagaTopBar(modifier) {
        ProfileButton(
            text = if (hasActiveRental == true) {
                stringResource(R.string.has_active_rental)
            } else {
                stringResource(R.string.pair_now).uppercase()
            },
            onClick = onPairingStartClick,
            isLoading = hasActiveRental == null,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        MainScreen(MainScreenState(activeRental = null))
    }
}