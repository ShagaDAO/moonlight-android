package com.limelight.shaga.ui.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R

@Composable
fun ProfileButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileButton(onClick, modifier, isLoading) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
fun ProfileBalanceButton(balance: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ProfileButton(onClick, modifier) {
        Text(
            balance,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
            maxLines = 1
        )
        Icon(
            painterResource(R.drawable.ic_solana_32dp),
            "Solana",
            tint = Color.Gray,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(16.dp)
        )
    }
}

@Composable
private fun ProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        contentColor = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(0.5.dp, ShagaColors.ButtonOutline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp)
        ) {
            if (!isLoading) {
                content()
            } else {
                ShagaProgressIndicator(
                    color = LocalContentColor.current,
                    size = ProgressBarSize.SMALL_16,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = ShagaColors.ButtonOutline,
                modifier = Modifier
                    .padding(start = 3.dp, end = 6.dp)
                    .height(24.dp)
                    .width(0.5.dp)
            )
            Image(
                painterResource(R.drawable.ic_profile_placeholder_32dp),
                "Profile",
                modifier = Modifier
                    .padding(end = 1.dp)
                    .size(32.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileButton(text = "Pair now".uppercase(), isLoading = false, onClick = {})
            ProfileBalanceButton(balance = "100,00", onClick = {})
        }
    }
}