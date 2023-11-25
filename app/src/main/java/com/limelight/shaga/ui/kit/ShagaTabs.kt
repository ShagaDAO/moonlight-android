package com.limelight.shaga.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ShagaTabs(
    titles: List<String>,
    selectedIndex: Int,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ShagaColors.TabBackground,
                shape = RoundedCornerShape(40.dp)
            )
            .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
    ) {
        titles.forEachIndexed { index, title ->
            Tab(
                title,
                isSelected = index == selectedIndex,
                onClick = { onClick(index) },
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Tab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        title,
        textAlign = TextAlign.Center,
        color = if (isSelected) ShagaColors.Accent else ShagaColors.TextSecondary,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = ShagaColors.TabBackgroundSelected,
                        shape = RoundedCornerShape(40.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(40.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Preview
@Composable
private fun Preview() {
    ShagaTheme {
        ShagaTabs(
            titles = listOf("Pair Now", "Game History"),
            selectedIndex = 0,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}