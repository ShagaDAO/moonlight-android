package com.limelight.shaga.ui.kit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ShagaDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ShagaColors.Background,
            contentColor = Color.White,
            border = BorderStroke(1.dp, ShagaColors.DialogBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}
