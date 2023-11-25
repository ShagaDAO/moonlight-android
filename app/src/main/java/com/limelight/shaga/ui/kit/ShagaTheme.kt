package com.limelight.shaga.ui.kit

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.limelight.R

@Composable
fun ShagaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            content()
        }
    }
}

private val colorScheme = lightColorScheme(
    primary = ShagaColors.Primary,
    onPrimary = Color.White,
    background = ShagaColors.Background,
    onBackground = Color.White,
    surface = ShagaColors.TagBackground,
    onSurface = Color.White,
    outline = ShagaColors.ButtonOutline
)

object ShagaColors {
    val Primary = Color(0x9600FF83)
    val Accent = Color(0xFF29B080)
    val Accent2 = Color(0xFF00FF8D)
    val Accent3 = Color(0xFF0D6B38)
    val Background = Color(0xFF181818)
    val TopBarBackground = Color(0xFF131314)
    val TopBarDivider = Color(0xFF1D1F1F)
    val ButtonOutline = Color(0x26FFFFFF)
    val TabBackground = Color(0x4D747474)
    val TabBackgroundSelected = Color(0xFF162724)
    val TextSecondary = Color(0xFF929298)
    val TextSecondary2 = Color(0x99FFFFFF)
    val TextSecondary3 = Color(0xFF868686)
    val TagBackground = Color(0x800C0C0E)
    val Online = Color(0xFF63E834)
    val DialogBorder = Color(0xFF0FB36D)
    val GameTileBackground = Color(0xB2111111)
    val SliderTrack = Color(0xFF6A6A6A)
}

object ShagaFontFamily {
    val outfit = FontFamily(
        Font(R.font.outfit_thin, FontWeight.Thin),
        Font(R.font.outfit_extralight, FontWeight.ExtraLight),
        Font(R.font.outfit_light, FontWeight.Light),
        Font(R.font.outfit_regular, FontWeight.Normal),
        Font(R.font.outfit_medium, FontWeight.Medium),
        Font(R.font.outfit_semibold, FontWeight.SemiBold),
        Font(R.font.outfit_bold, FontWeight.Bold),
        Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
        Font(R.font.outfit_black, FontWeight.Black),
    )
}

private val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ShagaFontFamily.outfit,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
)