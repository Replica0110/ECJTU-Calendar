package com.lonx.ecjtu.calendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme


@Composable
fun CalendarTheme(
    colorMode: Int = 0,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    MiuixTheme(
        colors = when (colorMode) {
            1 -> lightColorScheme()
            2 -> darkColorScheme()
            else -> if (darkTheme) darkColorScheme() else lightColorScheme()
        },
        content = {
            content()
        }
    )
}