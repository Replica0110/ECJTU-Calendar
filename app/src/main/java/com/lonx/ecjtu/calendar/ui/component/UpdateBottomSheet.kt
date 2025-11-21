package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.data.dto.UpdateDTO
import com.lonx.ecjtu.calendar.data.model.DownloadState
import com.lonx.ecjtu.calendar.util.UpdateState
import dev.jeziellago.compose.markdowntext.MarkdownText
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

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
            }
        ) {
            // 内容区：安全读取 updateDTO 的字段（使用安全调用和默认文本）

            val sizeText = updateState.updateDTO?.size ?: 0L
            if (sizeText != 0L) {
                Text(
                    text = "安装包大小：${sizeText / 1024 / 1024}MB",
                    style = MiuixTheme.textStyles.footnote1,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 更新日志
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            ) {
                MarkdownText(
                    markdown = updateState.updateDTO?.releaseNotes ?: "更新内容未提供",
                    style = MiuixTheme.textStyles.body2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 下载状态视图
            when (val ds2 = updateState.downloadState) {
                is DownloadState.InProgress -> {
                    val p = (ds2.progress.coerceIn(0, 100)) / 100f

                    ProgressButton(
                        text = "下载中 ${ds2.progress}%",
                        progress = p,
                        onClick = {
                            onCancelDownload()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }


                is DownloadState.Error -> {
                    Text(
                        text = "下载失败：${ds2.exception.message ?: "未知错误"}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        style = MiuixTheme.textStyles.footnote1
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        text = "重试",
                        onClick = {
                            onDownload()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is DownloadState.Success -> {

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        text = "安装更新",
                        onClick = {
                            onInstall()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }

                DownloadState.Idle -> {
                    TextButton(
                        text = "下载更新",
                        onClick = {
                            onDownload()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProgressButton(
    text: String,
    progress: Float?,       // null = 普通按钮；非 null = 显示进度条（0f~1f）
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val contentColor = MiuixTheme.colorScheme.onBackground

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled && (progress == null)) {
                onClick()
            }
    ) {

        if (progress != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.secondaryVariant)
            )

            // 已下载部分填充
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MiuixTheme.colorScheme.primaryVariant)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MiuixTheme.textStyles.main
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateBottomSheetPreview() {
    // 创建预览用的模拟数据
    val updateDTO = UpdateDTO(
        versionName = "v2.1.0",
        downloadUrl = "https://example.com/update.apk",
        releaseNotes = """
            ## 更新内容

            - 修复了一些已知问题
            - 优化了界面显示效果
            - 提升了性能表现

            感谢您的使用！
        """.trimIndent(),
        size = 18215242
    )

    val updateStateIdle = UpdateState(
        isChecking = false,
        updateDTO = updateDTO,
        downloadState = DownloadState.Idle,
        info = null
    )

    MiuixTheme {
        Scaffold(
        ) {
            Box(modifier = Modifier.padding(it)) {
                UpdateBottomSheet(
                    updateState = updateStateIdle,
                    onDismiss = {},
                    onDownload = {},
                    onCancelDownload = {},
                    onInstall = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateBottomSheetDownloadingPreview() {
    // 创建预览用的模拟数据
    val updateDTO = UpdateDTO(
        versionName = "v2.1.0",
        downloadUrl = "https://example.com/update.apk",
        releaseNotes = """
            ## 更新内容

            - 修复了一些已知问题
            - 优化了界面显示效果
            - 提升了性能表现

            感谢您的使用！
        """.trimIndent(),
        size = 1024 * 1024 * 10
    )

    val updateStateDownloading = UpdateState(
        isChecking = false,
        updateDTO = updateDTO,
        downloadState = DownloadState.InProgress(15),
        info = null
    )

    MiuixTheme {
        Scaffold() {
            Box(modifier = Modifier.padding(it)) {
                UpdateBottomSheet(
                    updateState = updateStateDownloading,
                    onDismiss = {},
                    onDownload = {},
                    onCancelDownload = {},
                    onInstall = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateBottomSheetSuccessPreview() {
    // 创建预览用的模拟数据
    val updateDTO = UpdateDTO(
        versionName = "v2.1.0",
        downloadUrl = "https://example.com/update.apk",
        releaseNotes = """
            ## 更新内容

            - 修复了一些已知问题
            - 优化了界面显示效果
            - 提升了性能表现

            感谢您的使用！
        """.trimIndent(),
        size = 1024
    )

    val updateStateSuccess = UpdateState(
        isChecking = false,
        updateDTO = updateDTO,
        downloadState = DownloadState.Success(File("")),
        info = null
    )

    MiuixTheme {
        Scaffold() {
            Box(modifier = Modifier.padding(it)) {
                UpdateBottomSheet(
                    updateState = updateStateSuccess,
                    onDismiss = {},
                    onDownload = {},
                    onCancelDownload = {},
                    onInstall = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateBottomSheetErrorPreview() {
    // 创建预览用的模拟数据
    val updateDTO = UpdateDTO(
        versionName = "v2.1.0",
        downloadUrl = "https://example.com/update.apk",
        releaseNotes = """
            ## 更新内容

            - 修复了一些已知问题
            - 优化了界面显示效果
            - 提升了性能表现

            感谢您的使用！
        """.trimIndent(),
        size = 1024
    )

    val updateStateError = UpdateState(
        isChecking = false,
        updateDTO = updateDTO,
        downloadState = DownloadState.Error(Exception("网络连接失败")),
        info = null
    )

    MiuixTheme {
        Scaffold() {
            Box(modifier = Modifier.padding(it)) {
                UpdateBottomSheet(
                    updateState = updateStateError,
                    onDismiss = {},
                    onDownload = {},
                    onCancelDownload = {},
                    onInstall = {}
                )
            }
        }
    }
}