package com.limelight.shaga.ui.pairing.session

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.ProgressBarSize
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaProgressIndicator
import com.limelight.shaga.ui.kit.SliderWithLabel
import com.limelight.shaga.ui.pairing.session.composables.NextButton
import com.limelight.shaga.ui.pairing.session.composables.SettingSection

@Composable
fun PaymentStepContent(
    state: SessionSettingsUiState.Payment,
    modifier: Modifier = Modifier,
    onTimeSliderChange: (Float) -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.title_time_slider),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 21.sp)
        )
        SliderWithLabel(
            value = state.sliderValue,
            onValueChange = onTimeSliderChange,
            label = state.sliderLabel,
            endLabel = state.sliderMaxLabel
        )
        val labelWidth = 64.dp
        val sectionModifier = Modifier.padding(top = 7.dp, start = 12.dp)
        SettingSection(
            label = stringResource(R.string.label_cpu),
            icon = painterResource(R.drawable.ic_cpu_32dp),
            value = state.data.cpuName,
            labelWidth = labelWidth,
            modifier = sectionModifier
        )
        SettingSection(
            label = stringResource(R.string.label_gpu),
            icon = painterResource(R.drawable.ic_gpu_32dp),
            value = state.data.gpuName,
            labelWidth = labelWidth,
            modifier = sectionModifier
        )
        SettingSection(
            label = stringResource(R.string.label_ram),
            icon = painterResource(R.drawable.ic_ram_32dp),
            value = "${state.data.totalRamMb / 1024u}GB",
            labelWidth = labelWidth,
            modifier = sectionModifier
        )
        SettingSection(
            label = stringResource(R.string.label_rating).uppercase(),
            icon = painterResource(R.drawable.ic_thumb_up),
            value = "93%  152 Reviews",
            labelWidth = labelWidth,
            modifier = sectionModifier
        )
        PriceSection(
            label = stringResource(R.string.label_total_price),
            value = stringResource(R.string.balance_value_format, state.totalPriceSol),
            isLoading = false,
            modifier = Modifier.padding(top = 20.dp)
        )
        PriceSection(
            label = stringResource(R.string.label_balance),
            value = stringResource(R.string.balance_value_format, state.accountBalanceSol ?: 0.0),
            isLoading = state.accountBalanceSol == null,
            modifier = Modifier.padding(top = 11.dp)
        )
        NextButton(
            text = stringResource(R.string.pay_now).uppercase(),
            onClick = onConfirmClick,
            isLoading = state.isMakingTransaction,
            modifier = Modifier.padding(top = 11.dp)
        )
    }
}

@Composable
private fun PriceSection(
    label: String,
    value: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            modifier = Modifier
                .padding(start = 12.dp)
                .width(90.dp)
        )
        if (isLoading) {
            ShagaProgressIndicator(
                color = LocalContentColor.current,
                size = ProgressBarSize.SMALL_16
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp)
            )
        }
        Icon(
            painterResource(R.drawable.ic_solana_32dp),
            null,
            tint = ShagaColors.TextSecondary2,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(17.dp)
        )
    }
}