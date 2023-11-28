package com.limelight.shaga.ui.main.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.limelight.R
import com.limelight.shaga.ui.kit.GameTileTag
import com.limelight.shaga.ui.kit.HeaderWithShowAll
import com.limelight.shaga.ui.kit.ProfilesRow
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTabs
import com.limelight.shaga.ui.kit.ShagaTheme

@Composable
fun GameListScreen() {
    Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 40.dp)) {
        ShagaTabs(
            titles = listOf("Games to Rent", "Your Library"),
            selectedIndex = 0,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        HeaderWithShowAll(
            title = "List of Games",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 10.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 162.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 40.dp),
            modifier = Modifier.weight(1f)
        ) {
            repeat(16) {
                item { GameTile(title = "Decentraland", imageUrl = null) }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GameTile(title: String, imageUrl: String?, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ShagaColors.GameTileBackground,
        contentColor = Color.White,
        modifier = modifier
            .width(162.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ShagaColors.ButtonOutline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(133.dp)
            ) {
                if (imageUrl == null) {
                    Image(
                        painterResource(R.drawable.decentraland),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    GlideImage(
                        imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                GameTileTag(
                    icon = {
                        Box {
                            Icon(
                                painterResource(R.drawable.ic_profile),
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Box(
                                Modifier
                                    .size(3.dp)
                                    .background(ShagaColors.Online, CircleShape)
                                    .align(Alignment.BottomEnd),
                                content = {}
                            )
                        }
                    },
                    text = "+999",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
                GameTileTag(
                    icon = {
                        Icon(
                            painterResource(R.drawable.ic_thumb_up),
                            null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    text = "96%",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                    modifier = Modifier.weight(1f)
                )
                ProfilesRow(itemSize = 12.dp)
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        Box(Modifier.fillMaxSize()) {
            Image(
                painterResource(id = R.drawable.content_background),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            GameListScreen()
        }
    }
}