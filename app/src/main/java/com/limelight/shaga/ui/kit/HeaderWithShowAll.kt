package com.limelight.shaga.ui.kit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.limelight.R

@Composable
fun HeaderWithShowAll(
    title: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    showAllText: String = stringResource(R.string.show_all)
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            fontSize = fontSize,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            showAllText,
            color = ShagaColors.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.clickable { }
        )
    }
}