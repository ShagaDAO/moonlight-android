package com.limelight.shaga.ui.pairing.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.limelight.R
import com.limelight.shaga.ui.kit.HeaderWithShowAll
import com.limelight.shaga.ui.kit.ShagaTheme
import com.limelight.shaga.ui.pairing.session.composables.NextButton
import com.limelight.shaga.ui.pairing.session.composables.SettingSection

@Composable
fun SessionInfoContent(
    state: SessionSettingsUiState.Info,
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {}
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.session_settings),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 21.sp)
        )

        when (state) {
            SessionSettingsUiState.Info.InvalidKey -> {
                Text(
                    stringResource(R.string.error_invalid_pairing),
                    modifier = Modifier.padding(24.dp)
                )
            }

            SessionSettingsUiState.Info.Error -> {
                Text(
                    stringResource(R.string.error_data_loading),
                    modifier = Modifier.padding(24.dp)
                )
            }

            SessionSettingsUiState.Info.Loading -> {
                CircularProgressIndicator(Modifier.padding(vertical = 24.dp))
            }

            is SessionSettingsUiState.Info.Display -> {
                SessionInfoDisplay(state, onNextClick)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun SessionInfoDisplay(
    state: SessionSettingsUiState.Info.Display,
    onNextClick: () -> Unit
) {
    SettingSection(
        label = stringResource(R.string.price),
        icon = painterResource(R.drawable.ic_price_32dp),
        value = "${state.usdPricePerHour}\$/Hr",
        isLoading = state.usdPricePerHour == null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
    )
    SettingSection(
        label = stringResource(R.string.exp_latency),
        icon = painterResource(R.drawable.ic_latency_32dp),
        value = "${state.expectedLatency}ms",
        isLoading = state.expectedLatency == null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 9.dp)
    )
    SettingSection(
        label = stringResource(R.string.gpu_tier),
        icon = painterResource(R.drawable.ic_gpu_32dp),
        value = "S",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 9.dp)
    )
    if (state.gameIconUrls.isNotEmpty()) {
        HeaderWithShowAll(
            title = stringResource(R.string.game_list),
            fontSize = 13.sp,
            showAllText = stringResource(R.string.see_all),
            modifier = Modifier.padding(top = 9.dp, end = 8.dp)
        )
        LazyRow(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.gameIconUrls) { url ->
                GlideImage(
                    url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(72.dp)
                        .height(105.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
    NextButton(
        text = stringResource(R.string.next).uppercase(),
        onClick = onNextClick,
        modifier = Modifier.padding(top = 21.dp)
    )
}

@Preview
@Composable
private fun LoadingStatePreview() {
    ShagaTheme {
        SessionInfoContent(state = SessionSettingsUiState.Info.Loading)
    }
}

@Preview
@Composable
private fun InvalidKeyPreview() {
    ShagaTheme {
        SessionInfoContent(state = SessionSettingsUiState.Info.InvalidKey)
    }
}