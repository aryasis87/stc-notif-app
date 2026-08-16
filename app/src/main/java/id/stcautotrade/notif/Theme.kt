package id.stcautotrade.notif

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Inter (variable) sebagai pengganti SF Pro — memberi rasa "Apple".
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun interW(w: Int) = Font(
    R.font.inter,
    weight = FontWeight(w),
    variationSettings = FontVariation.Settings(FontVariation.weight(w)),
)
private val Inter = FontFamily(interW(400), interW(500), interW(600), interW(700))

// Palet presisi bergaya iOS (systemGroupedBackground / secondary / label / separator).
object AppColors {
    val bgLight = Color(0xFFF2F2F7)
    val cardLight = Color(0xFFFFFFFF)
    val labelLight = Color(0xFF1C1C1E)
    val label2Light = Color(0xFF6E6E73)
    val sepLight = Color(0xFFD9D9DE)

    val bgDark = Color(0xFF000000)
    val cardDark = Color(0xFF1C1C1E)
    val labelDark = Color(0xFFFFFFFF)
    val label2Dark = Color(0xFF98989F)
    val sepDark = Color(0xFF38383A)

    val accentLight = Color(0xFFBD5B3A)
    val accentDark = Color(0xFFE58A6D)

    // semantik iOS
    val green = Color(0xFF34C759)
    val red = Color(0xFFFF3B30)
    val blue = Color(0xFF0A84FF)
    val amber = Color(0xFFFF9F0A)
}

private val LightScheme = lightColorScheme(
    primary = AppColors.accentLight, onPrimary = Color.White,
    background = AppColors.bgLight, onBackground = AppColors.labelLight,
    surface = AppColors.cardLight, onSurface = AppColors.labelLight,
    surfaceVariant = AppColors.cardLight, onSurfaceVariant = AppColors.label2Light,
    outline = AppColors.sepLight, outlineVariant = AppColors.sepLight,
    error = AppColors.red,
)
private val DarkScheme = darkColorScheme(
    primary = AppColors.accentDark, onPrimary = Color(0xFF241009),
    background = AppColors.bgDark, onBackground = AppColors.labelDark,
    surface = AppColors.cardDark, onSurface = AppColors.labelDark,
    surfaceVariant = AppColors.cardDark, onSurfaceVariant = AppColors.label2Dark,
    outline = AppColors.sepDark, outlineVariant = AppColors.sepDark,
    error = AppColors.red,
)

private fun t(w: Int, size: Int, line: Int, ls: Double = 0.0) =
    TextStyle(fontFamily = Inter, fontWeight = FontWeight(w), fontSize = size.sp, lineHeight = line.sp, letterSpacing = ls.sp)

private val AppType = Typography(
    displaySmall = t(700, 30, 36, -0.6),   // large title
    headlineMedium = t(700, 23, 28, -0.4), // title
    titleLarge = t(600, 19, 24, -0.2),
    titleMedium = t(600, 16, 21, -0.1),
    bodyLarge = t(400, 16, 21),
    bodyMedium = t(400, 14, 19),
    labelLarge = t(600, 16, 20),
    bodySmall = t(400, 13, 17),
    labelSmall = t(500, 12, 15, 0.2),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun STCNotifTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        typography = AppType,
        shapes = AppShapes,
        content = content,
    )
}
