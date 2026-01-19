package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.delay

/**
 * Miuix 风格的 Toast 组件，跟随主题色
 *
 * @param message 要显示的消息，为 null 时不显示
 * @param duration 显示时长（毫秒），默认 2000ms
 * @param onDismiss 当 Toast 完全消失后的回调
 */
@Composable
fun MiuixToast(
    message: String?,
    duration: Long = 2000,
    onDismiss: () -> Unit = {}
) {
    // 使用 key 确保每个新消息都重新创建状态
    key(message) {
        if (message != null) {
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(message) {
                isVisible = true
                delay(duration)
                isVisible = false
                // 等待退出动画完成（300ms）
                delay(300)
                onDismiss()
            }

            ToastContent(message = message, visible = isVisible)
        }
    }

    if (LocalInspectionMode.current && message != null) {
        ToastContent(message = message, visible = true)
    }
}

@Composable
private fun ToastContent(message: String, visible: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(
                        color = MiuixTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onPrimary
                )
            }
        }
    }
}