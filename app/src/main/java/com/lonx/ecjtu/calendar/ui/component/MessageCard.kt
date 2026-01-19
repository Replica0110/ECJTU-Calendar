package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

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
    // 从 MiuixTheme 获取主题色
    val colorScheme = MiuixTheme.colorScheme

    // 根据类型选择背景和文字颜色
    val backgroundColor = when (type) {
        MessageType.Error -> colorScheme.errorContainer
        MessageType.Warning -> colorScheme.surfaceVariant
        MessageType.Info -> colorScheme.tertiaryContainer
    }

    val textColor = when (type) {
        MessageType.Error -> colorScheme.onErrorContainer
        MessageType.Warning -> colorScheme.error
        MessageType.Info -> colorScheme.onTertiaryContainer
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
                style = MiuixTheme.textStyles.body1
            )
        }
    }
}

/**
 * MessageCard 组件预览
 */
@Preview(showBackground = true)
@Composable
fun MessageCardPreview() {
    CalendarTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MessageCard(
                message = "今天没有课啦~",
                type = MessageType.Info
            )
            MessageCard(
                message = "这是一个警告消息",
                type = MessageType.Warning
            )
            MessageCard(
                message = "这是一个错误消息",
                type = MessageType.Error
            )
        }
    }
}