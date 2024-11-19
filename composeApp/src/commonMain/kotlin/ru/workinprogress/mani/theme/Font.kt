package ru.workinprogress.mani.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import mani.composeapp.generated.resources.JetBrainsMono_Medium
import mani.composeapp.generated.resources.JetBrainsMono_Regular
import mani.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font


@Composable
fun fontFamily() = FontFamily(
    Font(resource = Res.font.JetBrainsMono_Regular, FontWeight.W400),
//    Font(resource = Res.font.JetBrainsMono_Light, FontWeight.W300),
    Font(resource = Res.font.JetBrainsMono_Medium, FontWeight.W500),
//    Font(resource = Res.font.JetBrainsMono_SemiBold, FontWeight.W600),
//    Font(resource = Res.font.JetBrainsMono_Bold, FontWeight.W700),
//    Font(resource = Res.font.JetBrainsMono_ExtraBold, FontWeight.W800),
)

fun createTypography(parent: Typography, fontFamily: FontFamily): Typography {
    return Typography(
        parent.displayLarge.copy(fontFamily = fontFamily),
        parent.displayMedium.copy(fontFamily = fontFamily),
        parent.displaySmall.copy(fontFamily = fontFamily),
        parent.headlineLarge.copy(fontFamily = fontFamily),
        parent.headlineMedium.copy(fontFamily = fontFamily),
        parent.headlineSmall.copy(fontFamily = fontFamily),
        parent.titleLarge.copy(fontFamily = fontFamily),
        parent.titleMedium.copy(fontFamily = fontFamily),
        parent.titleSmall.copy(fontFamily = fontFamily),
        parent.bodyLarge.copy(fontFamily = fontFamily),
        parent.bodyMedium.copy(fontFamily = fontFamily),
        parent.bodySmall.copy(fontFamily = fontFamily),
        parent.labelLarge.copy(fontFamily = fontFamily),
        parent.labelMedium.copy(fontFamily = fontFamily),
        parent.labelSmall.copy(fontFamily = fontFamily)
    )
}


