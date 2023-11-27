package com.limelight.shaga.ui.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.limelight.R

@Composable
fun ShagaTopBar(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
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
            content()
        }

        Divider(thickness = 1.dp, color = ShagaColors.TopBarDivider)
    }
}