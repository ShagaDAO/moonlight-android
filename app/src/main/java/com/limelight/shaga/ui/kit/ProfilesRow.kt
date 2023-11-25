package com.limelight.shaga.ui.kit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R

@Composable
fun ProfilesRow(itemSize: Dp, modifier: Modifier = Modifier) {
    val itemOffset = if (itemSize.value > 12) 12 else 8
    Box(modifier) {
        repeat(3) { index ->
            Image(
                painterResource(R.drawable.ic_profile_placeholder_32dp),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = (index * itemOffset).dp)
                    .size(itemSize)
                    .border(1.dp, Color.Black, CircleShape)
            )
        }
        ExtraBadge(
            text = "+344",
            size = itemSize,
            modifier = Modifier.padding(start = (3 * itemOffset).dp)
        )
    }
}

@Composable
private fun ExtraBadge(text: String, size: Dp, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Image(painterResource(R.drawable.badge_bg_32dp), null, modifier = Modifier.size(size))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (size > 12.dp) 5.sp else 3.sp,
                color = ShagaColors.Accent
            )
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfilesRow(itemSize = 16.dp)
            ProfilesRow(itemSize = 12.dp)
        }
    }
}