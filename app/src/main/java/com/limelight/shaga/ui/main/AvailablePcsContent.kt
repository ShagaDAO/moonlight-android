package com.limelight.shaga.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.GameTileTag
import com.limelight.shaga.ui.kit.HeaderWithShowAll
import com.limelight.shaga.ui.kit.ProfilesRow
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTheme

@Composable
fun AvailablePcsContent(modifier: Modifier = Modifier) {
    Column(modifier) {
        HeaderWithShowAll(
            title = stringResource(R.string.pc_available),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { AvailablePcItem(tier = "A Tier") }
            item { AvailablePcItem(tier = "B Tier") }
            item { AvailablePcItem(tier = "S Tier") }
        }
    }
}

@Composable
private fun AvailablePcItem(tier: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Box(
            Modifier
                .width(104.dp)
                .height(75.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Image(
                painterResource(R.drawable.pc_background),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            GameTileTag(Modifier.padding(4.dp)) {
                Text(
                    text = tier,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = "Tom’s Laptop",
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.ic_profile_placeholder_32dp),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(15.dp)
                        .border(1.dp, Color.Black, CircleShape)
                )
                Text(
                    text = "Tom",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShagaColors.TextSecondary,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Icon(
                    painterResource(R.drawable.ic_thumb_up),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 7.dp)
                        .size(10.dp)
                )
                Text(
                    text = "96%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
            Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Games",
                    style = MaterialTheme.typography.labelMedium,
                    color = ShagaColors.TextSecondary
                )
                ProfilesRow(itemSize = 16.dp, Modifier.padding(start = 7.dp))
            }
        }
        Column(Modifier.padding(end = 2.dp)) {
            GameTileTag(modifier.clickable {  }) {
                Text(
                    text = stringResource(R.string.action_connect).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(
                    painterResource(R.drawable.ic_cloud),
                    null,
                    tint = ShagaColors.Accent2,
                    modifier = Modifier.size(19.dp)
                )
            }
            Row(
                Modifier.padding(top = 4.dp, start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_price_32dp),
                    contentDescription = null,
                    tint = ShagaColors.Accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "10$ /",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 5.dp)
                )
                Text(
                    text = "Hr",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = ShagaColors.TextSecondary3,
                    modifier = Modifier.padding(start = 1.dp)
                )
            }
            Row(
                Modifier.padding(top = 4.dp, start = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_latency_32dp),
                    contentDescription = null,
                    tint = ShagaColors.TextSecondary3,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "14ms",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        AvailablePcsContent(Modifier.padding(16.dp))
    }
}