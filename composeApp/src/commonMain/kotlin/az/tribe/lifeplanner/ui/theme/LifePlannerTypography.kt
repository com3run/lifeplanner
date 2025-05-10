package az.tribe.lifeplanner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import leanlifeplanner.composeapp.generated.resources.*
import leanlifeplanner.composeapp.generated.resources.Res

@Composable
fun AppFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Satoshi_Regular, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(Res.font.Satoshi_Medium, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(Res.font.Satoshi_Bold, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(Res.font.Satoshi_Black, weight = FontWeight.Black, style = FontStyle.Normal)
)

@Composable
fun LifePlannerTypography(): Typography {
    val fontFamily = AppFontFamily()
    return Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = fontFamily),
            displayMedium = displayMedium.copy(fontFamily = fontFamily),
            displaySmall = displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = titleLarge.copy(fontFamily = fontFamily),
            titleMedium = titleMedium.copy(fontFamily = fontFamily),
            titleSmall = titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = bodySmall.copy(fontFamily = fontFamily),
            labelLarge = labelLarge.copy(fontFamily = fontFamily),
            labelMedium = labelMedium.copy(fontFamily = fontFamily),
            labelSmall = labelSmall.copy(fontFamily = fontFamily)
        )
    }
}