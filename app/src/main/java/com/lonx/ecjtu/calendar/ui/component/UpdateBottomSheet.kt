package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.util.UpdateState
import dev.jeziellago.compose.markdowntext.MarkdownText
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

// TODO(优化布局)
@Composable
fun UpdateBottomSheet(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstall: () -> Unit,
) {
    // 本地 show 状态用于控制 SuperBottomSheet 的显示/隐藏（避免直接依赖 nullable DTO 导致重组时 NPE）
    val shouldShowFromState = updateState.updateDTO != null
    var showSheet by remember { mutableStateOf(shouldShowFromState) }

    // 用来检测 showSheet 从 true -> false 的变化，以便调用 onDismiss 回调（且只在用户主动关闭时调用）
    var previousShow by remember { mutableStateOf(showSheet) }

    // 当外部 updateState.updateDTO 变化时，同步本地 show
    LaunchedEffect(shouldShowFromState) {
        // 如果外部需要显示，则立即显示
        // 如果外部设置为隐藏（updateDTO == null），我们也同步隐藏（这不会触发 onDismiss，因为外部已经处理了）
        showSheet = shouldShowFromState
    }

    // 监听本地 showSheet 变化，判断是否需要触发 onDismiss 回调
    LaunchedEffect(showSheet) {
        // 如果之前显示、现在隐藏，并且外部仍然持有 updateDTO（说明是用户主动关闭弹窗），调用 onDismiss
        if (previousShow && !showSheet && updateState.updateDTO != null) {
            // call onDismiss on next recomposition to avoid racing with compose
            onDismiss()
        }
        previousShow = showSheet
    }

    // 只在需要展示时创建底部弹窗（避免无谓的布局）
    if (showSheet) {
        // 为了避免在回调中直接修改 updateState 导致 race，只在本地隐藏 sheet，
        // 然后通过 onDismiss 回调让外部处理清理（或外部可以选择稍后清理）。
        SuperBottomSheet(
            show = remember { mutableStateOf(true) },
            title = "发现新版本 ${updateState.updateDTO?.versionName ?: ""}",
            onDismissRequest = {
                // 用户拖拽或点击外部导致的 dismiss —— 先隐藏本地 sheet，再走 onDismiss 由外部处理状态清理
                showSheet = false
            },
            leftAction = {
                TextButton(onClick = {
                    // 先隐藏 UI，再由 LaunchedEffect 触发 onDismiss()
                    showSheet = false
                }) {
                    Text(text = "取消")
                }
            },
            rightAction = {
                val ds = updateState.downloadState
                when (ds) {
                    is DownloadState.InProgress -> {
                        IconButton(
                            onClick = {
                                onInstall()
                            }
                        ) {
                            Text(text = "取消下载")
                        }
                    }
                    is DownloadState.Success -> {
                        IconButton(
                            onClick = {
                                onInstall()
                            }
                        ) {
                            Text(text = "安装更新")
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = {
                                onDownload()
                            }
                        ) {
                            Text(text = "下载更新")
                        }
                    }
                }
            }
        ) {
            // 内容区：安全读取 updateDTO 的字段（使用安全调用和默认文本）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {

//                val sizeText = updateState.updateDTO?.size ?: ""
//                if (sizeText.isNotEmpty()) {
//                    Text(text = "包大小：$sizeText", style = MaterialTheme.typography.bodyMedium)
//                    Spacer(modifier = Modifier.height(8.dp))
//                }

                // 更新日志
                Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp)){
                    MarkdownText(
                        markdown = updateState.updateDTO?.releaseNotes ?: "更新内容未提供",
                        style = MiuixTheme.textStyles.main
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 下载状态视图
                when (val ds2 = updateState.downloadState) {
                    is DownloadState.InProgress -> {
                        val progressFloat = (ds2.progress.coerceIn(0, 100)) / 100f
                        LinearProgressIndicator(progress = progressFloat)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "下载中：${ds2.progress}%")
                    }
                    is DownloadState.Error -> {
                        Text(text = "下载失败：${ds2.exception}", color = MaterialTheme.colorScheme.error)
                    }
                    is DownloadState.Success -> {
                        Text(text = "下载已完成，准备安装")
                    }
                    DownloadState.Idle -> {
                        // nothing
                    }
                }
            }
        }
    }
}
