package com.limelight.shaga.ui.pairing.session

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.pairing.session.composables.NextButton

@Composable
fun WalletInfoContent(
    state: SessionSettingsUiState.Wallet,
    modifier: Modifier = Modifier,
    onQrCodeRequest: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.not_enough_balance),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 21.sp)
        )
        when (state) {
            is SessionSettingsUiState.Wallet.Display -> {
                DisplayData(state, onQrCodeRequest, onConfirmClick)
            }

            SessionSettingsUiState.Wallet.Error -> {
                Text(stringResource(R.string.error_wallet_info), Modifier.padding(vertical = 24.dp))
            }
        }

    }
}

@Composable
private fun DisplayData(
    state: SessionSettingsUiState.Wallet.Display,
    onQrCodeRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(R.drawable.ic_wallet_32dp), null, Modifier.size(30.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Row {
                Text(
                    text = stringResource(R.string.label_balance),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
                )
                Row(
                    Modifier
                        .padding(start = 4.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(state.key))
                            Toast
                                .makeText(
                                    context,
                                    "Public key copied to clipboard",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shortenKey(state.key),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        color = ShagaColors.TextSecondary2
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        "Copy",
                        tint = ShagaColors.TextSecondary2,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(12.dp)
                    )
                }
            }
            Text(
                text = "${
                    stringResource(
                        R.string.balance_value_format,
                        state.accountBalanceSol
                    )
                } SOL",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_price_32dp),
            null,
            tint = ShagaColors.TextSecondary2,
            modifier = Modifier.size(26.dp)
        )
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                text = stringResource(R.string.credit_needed),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
            )
            Text(
                text = "${stringResource(R.string.balance_value_format, state.priceSol)} SOL",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
    Button(
        onClick = onQrCodeRequest,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ShagaColors.Accent3),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = Modifier
            .padding(top = 12.dp)
            .height(24.dp)
    ) {
        Text(
            stringResource(R.string.pay_with_qr).uppercase(),
            style = MaterialTheme.typography.bodySmall
        )
    }
    state.qrCode?.fold(
        onSuccess = {
            Image(
                bitmap = it.asImageBitmap(),
                "qr",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(116.dp)
            )
        },
        onFailure = {
            Text("Could not generate QR code", Modifier.padding(16.dp))
        }
    )
    Text(
        text = "Start Streaming",
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
        modifier = Modifier.padding(top = 8.dp)
    )
    NextButton(
        text = "${stringResource(R.string.balance_value_format, state.priceSol)} SOL",
        onClick = onConfirmClick,
        modifier = Modifier.padding(top = 4.dp),
        isLoading = state.isRefreshingBalance || state.isMakingTransaction
    )
}

private fun shortenKey(value: String): String {
    val maxLength = 11

    return if (value.length <= maxLength) {
        value
    } else {
        value.take(4) + "..." + value.takeLast(4)
    }
}
