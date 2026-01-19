package com.lonx.ecjtu.calendar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

val LocalColorMode = compositionLocalOf { 0 }

val KeyColors: List<Pair<String, Color>> = listOf(
    "蓝色" to Color(0xFF3482FF),
    "绿色" to Color(0xFF36D167),
    "紫色" to Color(0xFF7C4DFF),
    "黄色" to Color(0xFFFFB21D),
    "橙色" to Color(0xFFFF5722),
    "粉色" to Color(0xFFE91E63),
    "青色" to Color(0xFF00BCD4),
)

fun keyColorFor(index: Int): Color? =
    if (index <= 0) null else KeyColors.getOrNull(index - 1)?.second

@Composable
fun CalendarTheme(
    colorMode: Int = 0,
    keyColor: Color? = null,
    content: @Composable () -> Unit
) {
    val controller = remember(colorMode, keyColor) {
        when (colorMode) {
            1 -> ThemeController(ColorSchemeMode.Light)
            2 -> ThemeController(ColorSchemeMode.Dark)
            3 -> ThemeController(ColorSchemeMode.MonetSystem, keyColor = keyColor)
            4 -> ThemeController(ColorSchemeMode.MonetLight, keyColor = keyColor)
            5 -> ThemeController(ColorSchemeMode.MonetDark, keyColor = keyColor)
            else -> ThemeController(ColorSchemeMode.System)
        }
    }

    MiuixTheme(
        controller = controller,
        content = {
            CompositionLocalProvider(LocalColorMode provides colorMode) {
                content()
            }
        }
    )
}
