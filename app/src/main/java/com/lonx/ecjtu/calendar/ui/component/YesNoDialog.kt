package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonType
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding

/**
 * 一个高度可定制的、带有 "Yes/No" (或 "Confirm/Cancel") 动作按钮的对话框。
 *
 * @param onConfirm 用户点击确认按钮时的回调。
 * @param onDismiss 用户通过点击对话框外部或取消按钮来关闭对话框时的回调。
 * @param properties Dialog 的属性，如是否可通过返回键关闭。
 * @param title 对话框的标题。
 * @param content 对话框的主要文本内容。
 * @param drawContent 一个可选的 Composable Lambda，用于在标题和按钮之间绘制自定义内容。
 * @param dismissButton 一个可选的 Composable Lambda，用于完全自定义取消/关闭按钮。
 * @param confirmButton 一个可选的 Composable Lambda，用于完全自定义确认按钮。
 * @param cancelText 取消按钮的默认文本。
 * @param confirmText 确认按钮的默认文本。
 */
@Composable
fun YesNoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    title: String,
    content: String? = null, // 允许 content 为空，只显示自定义 drawContent
    drawContent: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable RowScope.() -> Unit)? = null,
    confirmButton:  (@Composable RowScope.() -> Unit)? = null,
    cancelText: String = "取消",
    confirmText: String = "确认"
) {
    BasicDialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        DialogTitle(text = title)
        content?.let { ItemOuterTip(text = it) } // 只有在 content 不为 null 时才显示
        drawContent?.invoke()
        Row(
            modifier = Modifier.outerPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dismissButton?.invoke(this) ?: Button(
                onClick = onDismiss,
                text = cancelText,
                modifier = Modifier.weight(1f).background(Color.Transparent),
                type = ButtonType.Sub,
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.padding))

            confirmButton?.invoke(this) ?: Button(
                onClick = onConfirm,
                text = confirmText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}