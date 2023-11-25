package com.limelight.shaga.ui.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.limelight.shaga.ui.kit.ProfilesRow
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTabs
import com.limelight.shaga.ui.kit.ShagaTheme

@Composable
fun HomeScreen() {
    val horizontalPadding = 22.dp
    Column(Modifier.padding(top = 15.dp)) {
        Text(
            text = stringResource(R.string.title_games_of_the_week),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 17.sp),
            modifier = Modifier.padding(start = horizontalPadding, bottom = 12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GameCard(
                    image = R.drawable.game_placeholder1,
                    index = 1,
                    likeCount = 199,
                    title = "Star Atlas",
                    description = "Collaborate to survive in an open-world environment, by battling other characters",
                    showProfiles = true,
                    showStreamTag = true
                )
            }
            item {
                GameCard(
                    image = R.drawable.game_placeholder2,
                    index = 2,
                    likeCount = 199,
                    title = "Minecraft",
                    description = "Play with your friends and create cubic words together",
                    showProfiles = false,
                    showStreamTag = false
                )
            }
        }

        var selectedTab by remember { mutableIntStateOf(1) }
        ShagaTabs(
            titles = listOf("Pair Now", "Game History"),
            selectedIndex = selectedTab,
            onClick = { selectedTab = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 12.dp)
        )

        Crossfade(targetState = selectedTab, label = "") { selectedIndex ->
            if (selectedIndex == 0) {
                AvailablePcsContent(Modifier.padding(horizontal = 26.dp))
            } else {
                LatestGamePlaysContent(Modifier.padding(horizontal = 26.dp))
            }
        }

    }
}

@Composable
private fun GameCard(
    @DrawableRes image: Int,
    index: Int,
    likeCount: Int,
    title: String,
    description: String,
    showProfiles: Boolean,
    showStreamTag: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        contentColor = Color.White,
        modifier = modifier
            .clip(shape)
            .clickable { }
    ) {
        Box(
            modifier
                .width(303.dp)
                .height(266.dp)
        ) {
            Image(
                painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .align(Alignment.BottomStart),
                content = {}
            )
            GameTileTag(Modifier.padding(start = 10.dp, top = 6.dp)) {
                Text(
                    text = "#$index",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
            GameTileTag(
                Modifier
                    .padding(end = 18.dp, top = 7.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
                Image(
                    painterResource(R.drawable.ic_like_32dp),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(17.dp)
                )
            }
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp, end = 19.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 21.sp
                            ),
                        )
                        if (showProfiles) {
                            ProfilesRow(itemSize = 16.dp, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (showStreamTag) {
                        StreamTag()
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    ),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun StreamTag(modifier: Modifier = Modifier) {
    GameTileTag(modifier) {
        Text(
            text = "Stream".uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
        Icon(
            painterResource(R.drawable.ic_cloud),
            null,
            tint = ShagaColors.Accent2,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(19.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        Box(Modifier.fillMaxSize()) {
            HomeScreen()
        }
    }
}