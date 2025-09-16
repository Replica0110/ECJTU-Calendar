package com.lonx.ecjtu.calendar.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.BuildConfig
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.ui.component.InputDialog
import com.lonx.ecjtu.calendar.ui.component.YesNoDialog
import com.lonx.ecjtu.calendar.ui.viewmodels.SettingsViewModel
import com.lonx.ecjtu.calendar.util.UpdateManager
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemArrowType
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTip
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, UnstableSaltUiApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val screenState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val onEvent = viewModel::onEvent

    val updateManager: UpdateManager = koinInject()

//    val updateState by updateManager.state.collectAsStateWithLifecycle()

    // 收集并显示UI效果
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 此处可以添加用于显示输入对话框的状态
    var showWeiXinIdDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {
        TitleBar(
            onBack = onNavigateBack,
            text = "设置",
            showBackBtn = true
        )

        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = SaltTheme.colors.background)
                    .verticalScroll(rememberScrollState())
            ) {
                RoundedColumn {
                    ItemTip(text = "配置")

                    Item(
                        iconPaddingValues = PaddingValues(2.dp),
                        onClick = { showWeiXinIdDialog = true /* 打开输入对话框 */ },
                        text = "weiXinID设置",
                        iconPainter = painterResource(R.drawable.ic_id_24dp),
                        sub = "华交教务weiXinID"
                    )

                    Item(
                        iconPaddingValues = PaddingValues(2.dp),
                        onClick = {
                            Toast.makeText(context, "请长按桌面进行添加", Toast.LENGTH_LONG).show()
                        },
                        text = "桌面小组件",
                        iconPainter = painterResource(R.drawable.ic_pin_appwidget),
                        sub = "添加日历小组件到桌面"
                    )

                    ItemSwitcher(
                        iconPaddingValues = PaddingValues(2.dp),
                        onChange = { viewModel.onEvent(SettingsEvent.OnAutoUpdateCheckChanged(!uiState.isAutoUpdateCheckEnabled)) },
                        text = "自动检查更新",
                        iconPainter = painterResource(R.drawable.ic_update),
                        sub = "应用启动时检查新版本",
                        state = uiState.isAutoUpdateCheckEnabled
                    )

                    Item(
                        onClick = {
                            Toast.makeText(context, "正在检查更新...", Toast.LENGTH_SHORT).show()
                            updateManager.checkForUpdate()
                        },
                        iconPaddingValues = PaddingValues(2.dp),
                        text = "立即检查更新",
                        iconPainter = painterResource(R.drawable.ic_update),
                        sub =  "检查应用是否有新版本"
                    )
                    Item(
                        iconPaddingValues = PaddingValues(2.dp),
                        onClick = { showClearCacheDialog = true },
                        text = "清理应用缓存",
                        iconPainter = painterResource(R.drawable.ic_clear_cache),
                        sub = "缓存大小：${screenState.cacheSize}"
                    )
                }


                RoundedColumn {
                    ItemTip(text = "关于")

                    Item(
                        onClick = { },
                        iconPaddingValues = PaddingValues(2.dp),
                        text = "应用版本",
                        iconPainter = painterResource(R.drawable.ic_version_24dp),
                        sub = "版本：${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})\n编译时间：${BuildConfig.BUILD_TIME}",
                        arrowType = ItemArrowType.None
                    )

                    Item(
                        onClick = { openUrl(context, "https://github.com/Replica0110") },
                        iconPaddingValues = PaddingValues(2.dp),
                        text = "开发者",
                        iconPainter = painterResource(R.drawable.ic_developer_24dp),
                        sub = "Replica0110",
                        arrowType = ItemArrowType.Link
                    )

                    Item(
                        onClick = {
                            openUrl(
                                context,
                                "https://github.com/Replica0110/ECJTU-Calendar"
                            )
                        },
                        iconPaddingValues = PaddingValues(2.dp),
                        text = "项目源码",
                        iconPainter = painterResource(R.drawable.ic_github_24dp),
                        sub = "在 GitHub 上查看本项目",
                        arrowType = ItemArrowType.Link
                    )
                }
            }
        }


    }

    if (showWeiXinIdDialog) {
        var currentInput by remember(showWeiXinIdDialog) { mutableStateOf(uiState.weiXinId) }
        InputDialog(
            onDismissRequest = { showWeiXinIdDialog = false },
            title = "weiXinID设置",
            text = currentInput,
            onConfirm = {
                viewModel.onEvent(SettingsEvent.OnIdChange(currentInput))
                viewModel.onEvent(SettingsEvent.OnSaveClick)
                showWeiXinIdDialog = false
            },
            onChange = { currentInput = it},
            cancelText = "取消",
            confirmText = "保存"
        )
    }
    if (showClearCacheDialog) {
        YesNoDialog(
            title = "确认操作",
            content = "您确定要清理所有应用缓存吗？\n这将永久删除已下载的安装包和其他临时文件",
            onConfirm = {
                onEvent(SettingsEvent.OnClearCacheClick)
                showClearCacheDialog = false
            },

            onDismiss = { showClearCacheDialog = false },
            confirmText = "清理"
        )
    }
//    if (updateState.updateInfo != null) {
//        val updateInfo = updateState.updateInfo!!
//
//        YesNoDialog(
//            title = "发现新版本 ${updateInfo.versionName}",
//
//            onDismiss = { }, //不允许通过点击空白处关闭
//
//            onConfirm = { },
//
//            content = null,
//
//            drawContent = {
//                RoundedColumn(modifier = Modifier
//                    .fillMaxWidth()
//                    .heightIn(max = 300.dp)
//                    .verticalScroll(rememberScrollState())
//                ) {
//                    Text(
//                        modifier = Modifier.padding(8.dp),
//                        text = updateInfo.releaseNotes
//                    )
//                }
//            },
//
//            cancelText = "稍后",
//            dismissButton =  {
//                val (buttonText, onClickAction) = when (val state = updateState.downloadState) {
//                    is DownloadState.InProgress -> Pair("取消下载") {
//                        onEvent(SettingsEvent.DismissUpdateDialog)
//                        onEvent(SettingsEvent.CancelDownload)
//                    }
//                    else -> Pair("稍后") { onEvent(SettingsEvent.DismissUpdateDialog) }
//                }
//                Button(
//                    onClick = onClickAction,
//                    text = buttonText,
//                    modifier = Modifier.weight(1f),
//                    type = ButtonType.Sub,
//                )
//            },
//            confirmButton = {
//                val (buttonText, onClickAction) = when (val state = updateState.downloadState) {
//                    is DownloadState.Success -> Pair("安装") { onEvent(SettingsEvent.InstallUpdate) }
//                    is DownloadState.InProgress -> Pair("下载中 ${state.progress}%") {onEvent(SettingsEvent.CancelDownload)}
//                    is DownloadState.Error -> Pair("重试") { onEvent(SettingsEvent.StartDownload) }
//                    else -> Pair("下载") { onEvent(SettingsEvent.StartDownload) }
//                }
//
//                Button(
//                    onClick = onClickAction,
//                    text = buttonText,
//                    modifier = Modifier.weight(1f)
//                )
//            }
//        )
//    }
}

// 帮助函数，用于打开网页
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}