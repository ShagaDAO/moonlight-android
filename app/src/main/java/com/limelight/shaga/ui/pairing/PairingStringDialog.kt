package com.limelight.shaga.ui.pairing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaDialog
import com.limelight.shaga.ui.kit.ShagaTheme

@Composable
fun PairingStringDialog(onConfirmClick: (String) -> Unit, onDismissRequest: () -> Unit) {
    var value by remember { mutableStateOf("") }

    ShagaDialog(onDismissRequest) {
        Column(
            modifier = Modifier.padding(top = 24.dp, start = 38.dp, end = 38.dp, bottom = 19.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.pair_now),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 21.sp)
            )
            OutlinedTextField(
                value = value,
                onValueChange = {
                    val isPasteEvent = it.length - value.length > 10
                    value = it
                    if (isPasteEvent) {
                        onConfirmClick(value)
                        onDismissRequest()
                    }
                },
                shape = RoundedCornerShape(28.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.hint_insert_string),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_chevron_right),
                        null,
                        Modifier
                            .size(16.dp)
                            .clickable {
                                if (value.isNotEmpty()) {
                                    onConfirmClick(value)
                                    onDismissRequest()
                                }
                            }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedPlaceholderColor = ShagaColors.TextSecondary2,
                    unfocusedPlaceholderColor = ShagaColors.TextSecondary2,
                    focusedBorderColor = ShagaColors.TextSecondary2,
                    unfocusedBorderColor = ShagaColors.TextSecondary2,
                    focusedTrailingIconColor = ShagaColors.TextSecondary2,
                    unfocusedTrailingIconColor = ShagaColors.TextSecondary2,
                    focusedTextColor = ShagaColors.TextSecondary2,
                    unfocusedTextColor = ShagaColors.TextSecondary2
                ),
                singleLine = true,
                modifier = Modifier.padding(top = 22.dp)
            )
            Row(Modifier.padding(top = 9.dp)) {
                Text(
                    stringResource(R.string.pair_now_hint),
                    color = ShagaColors.TextSecondary2,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.click_here),
                    color = ShagaColors.DialogBorder,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .clickable { }
                        .padding(start = 2.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        PairingStringDialog(onConfirmClick = {}, onDismissRequest = {})
    }
}