package com.lonx.ecjtu.calendar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

val LocalColorMode = compositionLocalOf { 0 }

@Composable
fun CalendarTheme(
    colorMode: Int = 0,
    content: @Composable () -> Unit
) {
    val darkTheme = when (colorMode) {
        1 -> false // 浅色模式
        2 -> true  // 深色模式
        else -> isSystemInDarkTheme() // 跟随系统
    }
    
    MiuixTheme(
        colors = when (colorMode) {
            1 -> lightColorScheme()
            2 -> darkColorScheme()
            else -> if (darkTheme) darkColorScheme() else lightColorScheme()
        },
        content = {
            CompositionLocalProvider(LocalColorMode provides colorMode) {
                content()
            }
        }
    )
}