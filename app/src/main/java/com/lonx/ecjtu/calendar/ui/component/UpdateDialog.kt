package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.util.UpdateState
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ButtonType
import com.moriafly.salt.ui.RoundedColumn

// 我们使用之前在 SettingsScreen 中创建的 YesNoDialog 作为基础
@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: () -> Unit
) {
    val updateInfo = updateState.updateInfo ?: return // 如果没有信息，则不显示

    YesNoDialog(
        title = "发现新版本 ${updateInfo.versionName}",
        onDismiss = onDismiss,
        onConfirm = { /* 由 confirmButton 参数处理 */ },
        content = null, // 我们使用 drawContent 来自定义内容
        drawContent = {
            RoundedColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = updateInfo.releaseNotes
                )
            }
        },
        cancelText = "稍后",
        dismissButton =  {
            // 根据下载状态，动态改变“取消”按钮的行为
            val (buttonText, onClickAction) = when (updateState.downloadState) {
                is DownloadState.InProgress -> "取消下载" to onCancelDownload
                else -> "稍后" to onDismiss
            }
            Button(
                onClick = onClickAction,
                text = buttonText,
                modifier = Modifier.weight(1f),
                type = ButtonType.Sub
            )
        },
        confirmButton = {
            // 根据下载状态，动态改变“确认”按钮的行为
            val (buttonText, onClickAction, isEnabled) = when (val state = updateState.downloadState) {
                is DownloadState.Success -> Triple("安装", onInstall, true)
                is DownloadState.InProgress -> Triple("下载中 ${state.progress}%", {}, false)
                is DownloadState.Error -> Triple("重试", onDownload, true)
                else -> Triple("下载", onDownload, true)
            }
            Button(
                onClick = onClickAction,
                text = buttonText,
                enabled = isEnabled,
                modifier = Modifier.weight(1f),
                type = if (isEnabled) ButtonType.Highlight else ButtonType.Sub
            )
        }
    )
}