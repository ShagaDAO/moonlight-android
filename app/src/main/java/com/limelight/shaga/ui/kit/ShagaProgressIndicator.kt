package com.limelight.shaga.ui.kit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingDisplay(modifier: Modifier = Modifier, size: ProgressBarSize = ProgressBarSize.REGULAR) {
    Box(modifier, contentAlignment = Alignment.Center) {
        ShagaProgressIndicator(size = size)
    }
}

@Composable
fun ShagaProgressIndicator(
    modifier: Modifier = Modifier,
    size: ProgressBarSize = ProgressBarSize.REGULAR,
    color: Color = ProgressIndicatorDefaults.circularColor
) {
    when (size) {
        ProgressBarSize.SMALL_16 -> {
            CircularProgressIndicator(modifier.size(16.dp), color, strokeWidth = 2.dp)
        }
        ProgressBarSize.SMALL_20 -> {
            CircularProgressIndicator(modifier.size(20.dp), color, strokeWidth = 2.dp)
        }
        ProgressBarSize.SMALL_24 -> {
            CircularProgressIndicator(modifier.size(24.dp), color, strokeWidth = 2.dp)
        }
        ProgressBarSize.REGULAR -> {
            CircularProgressIndicator(modifier, color)
        }
    }
}

enum class ProgressBarSize {
    SMALL_16,
    SMALL_20,
    SMALL_24,
    REGULAR
}