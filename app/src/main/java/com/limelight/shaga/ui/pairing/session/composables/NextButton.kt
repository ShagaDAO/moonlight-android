package com.limelight.shaga.ui.pairing.session.composables

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.shaga.ui.kit.ProgressBarSize
import com.limelight.shaga.ui.kit.ShagaProgressIndicator

@Composable
fun NextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.defaultMinSize(minWidth = 78.dp, minHeight = 31.dp)
    ) {
        if (!isLoading) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
            )
        } else {
            ShagaProgressIndicator(
                color = Color.White,
                size = ProgressBarSize.SMALL_16
            )
        }
    }
}