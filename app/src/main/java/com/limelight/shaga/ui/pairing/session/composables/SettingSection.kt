package com.limelight.shaga.ui.pairing.session.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limelight.shaga.ui.kit.ShagaColors
import com.limelight.shaga.ui.main.InfoBadge

@Composable
fun SettingSection(
    label: String,
    icon: Painter,
    value: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp? = null,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp),
            modifier = if (labelWidth == null) Modifier.weight(1f) else Modifier.width(labelWidth)
        )
        Box(Modifier.weight(1f)) {
            InfoBadge(
                text = value,
                icon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = ShagaColors.TextSecondary3,
                        modifier = Modifier.size(18.dp)
                    )
                },
                color = ShagaColors.TextSecondary2,
                minWidth = 36.dp,
                isLoading = isLoading
            )
        }
    }
}