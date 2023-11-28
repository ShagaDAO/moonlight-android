package com.limelight.shaga.ui.main.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.R
import com.limelight.shaga.ui.kit.HeaderWithShowAll
import com.limelight.shaga.ui.kit.ProgressBarSize
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaProgressIndicator
import com.limelight.shaga.ui.kit.ShagaTheme

@Composable
fun LatestGamePlaysContent(modifier: Modifier = Modifier) {
    Column(modifier) {
        HeaderWithShowAll(
            title = stringResource(R.string.latest_gameplays),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(3) {
                item {
                    GamePlayItem(
                        background = R.drawable.game_placeholder1,
                        title = "Fortnite"
                    )
                }
                item {
                    GamePlayItem(
                        background = R.drawable.game_placeholder2,
                        title = "Minecraft"
                    )
                }
            }
        }
    }
}

@Composable
private fun GamePlayItem(
    @DrawableRes background: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        Image(
            painterResource(background),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(104.dp)
                .height(62.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
            InfoSection(
                text = stringResource(R.string.section_lender),
                value = "Tom",
                icon = {
                    Image(
                        painterResource(R.drawable.ic_profile_placeholder_32dp),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .size(15.dp)
                            .border(1.dp, Color.Black, CircleShape)
                    )
                }
            )
            InfoSection(
                text = stringResource(R.string.section_avg_ping),
                value = "14ms",
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_latency_32dp),
                        null,
                        tint = ShagaColors.TextSecondary,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(18.dp)
                    )
                }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InfoBadge(text = "2 Hr", icon = {
                Image(painterResource(R.drawable.ic_time), null, modifier = Modifier.size(18.dp))
            })
            InfoBadge(text = "31 $", icon = {
                Icon(
                    painterResource(R.drawable.ic_price_32dp),
                    contentDescription = null,
                    tint = ShagaColors.TextSecondary3,
                    modifier = Modifier.size(18.dp)
                )
            })
        }
    }
}

@Composable
private fun InfoSection(
    text: String,
    value: String,
    icon: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            color = ShagaColors.TextSecondary
        )
        icon()
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            color = ShagaColors.TextSecondary,
            modifier = Modifier.padding(start = 3.dp)
        )
    }
}

@Composable
fun InfoBadge(
    text: String,
    icon: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    color: Color = ShagaColors.TextSecondary3,
    minWidth: Dp = 50.dp,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .widthIn(min = minWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        if (!isLoading) {
            Text(
                text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(start = 3.dp)
            )
        } else {
            ShagaProgressIndicator(
                color = color,
                size = ProgressBarSize.SMALL_16,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        LatestGamePlaysContent()
    }
}