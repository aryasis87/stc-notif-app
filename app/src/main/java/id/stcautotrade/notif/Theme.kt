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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Palet bergaya iOS (grouped background + kartu putih + aksen clay merek).
object AppColors {
    // light
    val bgLight = Color(0xFFF2F2F7)
    val cardLight = Color(0xFFFFFFFF)
    val card2Light = Color(0xFFFFFFFF)
    val labelLight = Color(0xFF1C1C1E)
    val label2Light = Color(0xFF8E8E93)
    val sepLight = Color(0xFFE3E3E8)
    // dark
    val bgDark = Color(0xFF000000)
    val cardDark = Color(0xFF1C1C1E)
    val card2Dark = Color(0xFF2C2C2E)
    val labelDark = Color(0xFFFFFFFF)
    val label2Dark = Color(0xFF98989F)
    val sepDark = Color(0xFF38383A)
    // accent (clay merek)
    val accentLight = Color(0xFFBD5B3A)
    val accentDark = Color(0xFFE0805E)
    // semantik
    val green = Color(0xFF34C759)
    val red = Color(0xFFFF3B30)
    val blue = Color(0xFF0A84FF)
    val amber = Color(0xFFFF9F0A)
}

private val LightScheme = lightColorScheme(
    primary = AppColors.accentLight,
    onPrimary = Color.White,
    background = AppColors.bgLight,
    onBackground = AppColors.labelLight,
    surface = AppColors.cardLight,
    onSurface = AppColors.labelLight,
    surfaceVariant = AppColors.card2Light,
    onSurfaceVariant = AppColors.label2Light,
    outline = AppColors.sepLight,
    error = AppColors.red,
)

private val DarkScheme = darkColorScheme(
    primary = AppColors.accentDark,
    onPrimary = Color(0xFF241009),
    background = AppColors.bgDark,
    onBackground = AppColors.labelDark,
    surface = AppColors.cardDark,
    onSurface = AppColors.labelDark,
    surfaceVariant = AppColors.card2Dark,
    onSurfaceVariant = AppColors.label2Dark,
    outline = AppColors.sepDark,
    error = AppColors.red,
)

private val SF = FontFamily.Default // sistem (mendekati SF/Roboto)

private val AppType = Typography(
    // large title iOS
    displaySmall = TextStyle(fontFamily = SF, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = SF, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontFamily = SF, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = SF, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = SF, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = SF, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = SF, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = SF, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = SF, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
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
