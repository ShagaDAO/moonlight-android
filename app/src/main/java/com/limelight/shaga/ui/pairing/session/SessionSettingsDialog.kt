package com.limelight.shaga.ui.pairing.session

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaDialog
import com.limelight.shagaProtocol.PairingActivity


@Composable
fun SessionSettingsDialog(
    viewModel: SessionSettingsViewModel,
    onDismissRequest: () -> Unit,
    onOpenPairing: (clientString: String, ipAddress: String, authority: String) -> Unit
) {
    val collectedUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = collectedUiState

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.initWallet(context)

        viewModel.uiEvents.collect { event ->
            when (event) {
                SessionSettingsUiEvent.PaymentError -> {
                    Toast.makeText(context, "Transaction failed", Toast.LENGTH_LONG).show()
                }

                is SessionSettingsUiEvent.PaymentSuccess -> {
                    val data = event.data
                    //onOpenPairing(event.clientString, data.ipAddress, data.authority.toString())
                    PairingActivity.start(
                        context,
                        event.clientString,
                        data.ipAddress,
                        data.authority.toString()
                    )
                }
            }
        }
    }

    ShagaDialog(onDismissRequest) {
        Column(
            modifier = Modifier.padding(top = 13.dp, start = 34.dp, end = 34.dp, bottom = 19.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is SessionSettingsUiState.Info -> {
                    SessionInfoContent(
                        uiState,
                        onNextClick = viewModel::moveToNextStep,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is SessionSettingsUiState.Payment -> {
                    PaymentStepContent(
                        uiState,
                        onTimeSliderChange = viewModel::changeSliderValue,
                        onConfirmClick = viewModel::moveToNextStep
                    )
                }

                is SessionSettingsUiState.Wallet -> {
                    WalletInfoContent(
                        uiState,
                        onQrCodeRequest = viewModel::generateWalletQrCode,
                        onConfirmClick = viewModel::moveToNextStep
                    )
                }
            }

            StepIndicator(
                count = uiState.totalSteps,
                selected = uiState.stepNumber,
                Modifier.padding(top = 16.dp)
            )
        }
    }
}


@Composable
private fun StepIndicator(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(
                        color = if (index == selected) ShagaColors.Primary else Color.Gray,
                        shape = RoundedCornerShape(size = 4.dp)
                    ),
                content = {}
            )
        }
    }
}
