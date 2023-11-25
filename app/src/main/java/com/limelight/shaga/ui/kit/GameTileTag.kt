package com.limelight.shaga.ui.kit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameTileTag(
    icon: @Composable RowScope.() -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    GameTileTag(modifier) {
        icon()
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            modifier = Modifier.padding(start = 3.dp)
        )
    }
}

@Composable
fun GameTileTag(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = ShagaColors.TagBackground,
        contentColor = Color.White,
        shape = RoundedCornerShape(40.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            content()
        }
    }
}