package com.limelight.shaga.ui.main.games

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.limelight.AppView.AppObject
import com.limelight.R
import com.limelight.shaga.ui.kit.HeaderWithShowAll
import com.limelight.shaga.ui.kit.LoadingDisplay
import com.limelight.shaga.ui.kit.ProfileButton
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.kit.ShagaTabs
import com.limelight.shaga.ui.kit.ShagaTopBar
import com.limelight.shaga.ui.main.BottomNavigation
import com.limelight.shagaProtocol.MapPopulation

@Composable
fun GameListScreenCompat(uiState: GameListUiState?, onItemClick: (AppObject) -> Unit) {
    Scaffold(
        topBar = {
            ShagaTopBar(Modifier.fillMaxWidth()) {
                ProfileButton(
                    text = "2 Hr left",
                    onClick = {},
                    isLoading = false,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        bottomBar = { BottomNavigation() },
        containerColor = ShagaColors.Background,
        contentColor = Color.White,
        modifier = Modifier.navigationBarsPadding()
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Image(
                painterResource(id = R.drawable.content_background),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            Content(uiState, onItemClick, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun Content(
    uiState: GameListUiState?,
    onItemClick: (AppObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(start = 22.dp, end = 22.dp, top = 40.dp)) {
        ShagaTabs(
            titles = listOf("Games to Rent", "Your Library"),
            selectedIndex = 0,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState != null) {
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
                items(uiState.items) { item ->
                    GameTile(
                        item.app.appName,
                        imageUrl = MapPopulation.NetworkUtils.buildGameIconUrl(
                            uiState.ipAddress,
                            item.app.appId
                        ),
                        onClick = { onItemClick(item) }
                    )
                }
            }
        } else {
            LoadingDisplay(Modifier.fillMaxSize())
        }
    }
}
