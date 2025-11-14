package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import com.lonx.ecjtu.calendar.ui.theme.LocalColorMode

/**
 * 消息类型枚举
 */
enum class MessageType {
    Error, Warning, Info
}

/**
 * 通用消息卡片组件
 */
@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    message: String,
    type: MessageType = MessageType.Info,
    onClick: (() -> Unit)? = null
) {
    // 从 CompositionLocal 获取当前的颜色模式
    val colorMode = LocalColorMode.current
    
    // 根据 colorMode 确定当前是否为深色主题
    val isDark = when (colorMode) {
        0 -> isSystemInDarkTheme() // 跟随系统
        1 -> false // 浅色主题
        2 -> true // 深色主题
        else -> isSystemInDarkTheme() // 默认跟随系统
    }

    // 根据类型选择背景和文字颜色
    val (backgroundColor, textColor) = when (type) {
        MessageType.Error -> (
                if (isDark) Color(0xFF310808) else Color(0xFFF8E2E2)
                ) to Color(0xFFF72727)
        MessageType.Warning -> (
                if (isDark) Color(0xFF3B2B07) else Color(0xFFFFF3CD)
                ) to Color(0xFFFFA000)
        MessageType.Info -> (
                if (isDark) Color(0xFF062B1A) else Color(0xFFE2F8E9)
                ) to Color(0xFF2E7D32)
    }

    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.defaultColors(color = backgroundColor),
        showIndication = onClick != null,
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}