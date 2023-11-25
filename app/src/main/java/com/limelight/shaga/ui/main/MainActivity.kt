package com.limelight.shaga.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limelight.R
import com.limelight.shaga.ui.kit.ProfileButton
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTheme
import com.limelight.shaga.ui.pairing.PairingStringDialog
import com.limelight.shaga.ui.pairing.session.SessionSettingsDialog
import com.limelight.shaga.ui.pairing.session.SessionSettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShagaTheme {
                val viewModel = viewModel<HomeViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                Content(uiState)
            }
        }
    }

    @Composable
    private fun Content(state: HomeState) {
        var isPairingDialogVisible by remember { mutableStateOf(false) }
        var pairingString by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopBar(
                    hasActiveRental = state.hasActiveRental,
                    onPairingStartClick = { isPairingDialogVisible = true },
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
                HomeScreen()
            }
        }

        if (isPairingDialogVisible) {
            PairingStringDialog(
                onConfirmClick = { pairingString = it },
                onDismissRequest = { isPairingDialogVisible = false }
            )
        }

        if (pairingString != null) {
            SessionSettingsDialog(
                viewModel = viewModel(key = pairingString) {
                    SessionSettingsViewModel(pairingString!!)
                },
                onDismissRequest = { pairingString = null }
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
        Column(modifier) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShagaColors.TopBarBackground)
                    .padding(top = 14.dp, start = 18.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.app_logo),
                        "logo",
                        modifier = Modifier.width(45.dp)
                    )
                    Text(
                        stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp)
                    )
                }
                ProfileButton(
                    text = if (hasActiveRental == true) {
                        stringResource(R.string.has_active_rental)
                    } else {
                        stringResource(R.string.pair_now).uppercase()
                    },
                    onClick = {
                        if (hasActiveRental == false) {
                            onPairingStartClick()
                        }
                    },
                    isLoading = hasActiveRental == null,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Divider(thickness = 1.dp, color = ShagaColors.TopBarDivider)
        }
    }

    @Preview
    @Composable
    private fun Preview() {
        ShagaTheme {
            Content(HomeState(hasActiveRental = false))
        }
    }

}