package com.limelight.shaga.ui.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.ProfileButton
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTheme
import com.limelight.shaga.ui.kit.ShagaTopBar

@Composable
fun ConnectionScreen(uiState: ConnectionScreenState, onRetryClick: () -> Unit) {
    Scaffold(
        containerColor = ShagaColors.Background2,
        contentColor = Color.White,
        topBar = {
            ShagaTopBar {
                ProfileButton(
                    text = "",
                    onClick = {},
                    isLoading = true,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(R.drawable.ic_laptop_100dp),
                null,
                tint = ShagaColors.TextSecondary2,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (uiState.isError) {
                TextButton(onClick = onRetryClick, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Retry")
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        ConnectionScreen(ConnectionScreenState("Connecting", false), onRetryClick = {})
    }
}