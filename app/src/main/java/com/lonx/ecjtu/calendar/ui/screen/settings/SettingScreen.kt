package com.lonx.ecjtu.calendar.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.BuildConfig
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.lonx.ecjtu.calendar.domain.model.SchedulePage
import com.lonx.ecjtu.calendar.ui.viewmodel.SettingsViewModel
import com.lonx.ecjtu.calendar.ui.widget.CourseGlanceWidget
import com.lonx.ecjtu.calendar.ui.widget.CourseUiState
import com.lonx.ecjtu.calendar.ui.widget.CourseWidgetReceiver
import com.lonx.ecjtu.calendar.ui.widget.CourseWidgetState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AcademicCalendarScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
@Destination<RootGraph>(label = "设置")
fun SettingScreen(
    topAppBarScrollBehavior: ScrollBehavior,
    navigator: DestinationsNavigator,
    focusManager: FocusManager
) {
    val viewModel: SettingsViewModel = koinViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val onEvent = viewModel::onEvent

    val showWeiXinIdDialog = remember { mutableStateOf(false) }
    val showClearCacheDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val windowSize = getWindowSize()
    SuperDialog(
        modifier = Modifier.padding(bottom = 16.dp),
        show = showWeiXinIdDialog,
        title = "weiXinID设置",
        onDismissRequest =  {
            showWeiXinIdDialog.value = false
        },
        content = {
            var currentInput by remember { mutableStateOf(uiState.weiXinId) }
            TextField(
                value = currentInput ,
                onValueChange = { currentInput  = it },
                modifier = Modifier.padding(bottom = 16.dp),
                minLines = 2,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    text = "取消",
                    onClick = {
                        showWeiXinIdDialog.value = false
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = "确定",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnIdChange(currentInput))
                        viewModel.onEvent(SettingsEvent.OnSaveClick)
                        showWeiXinIdDialog.value = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    )
    SuperDialog(
        modifier = Modifier.padding(bottom = 16.dp),
        show = showClearCacheDialog,
        title = "清除缓存",
        onDismissRequest =  {
            showClearCacheDialog.value = false
        },
        content = {
            Text(
                text = "您确定要清理所有应用缓存吗？\n这将永久删除已下载的安装包和其他临时文件",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    text = "取消",
                    onClick = {
                        showClearCacheDialog.value = false
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = "确定",
                    onClick = {
                        viewModel.onEvent(SettingsEvent.OnClearCacheClick)
                        showClearCacheDialog.value = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    )
    // 收集并显示UI效果
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is SettingsEffect.RequestPinAppWidgetClick -> {
                    try {
                        coroutineScope.launch {
                            val previewDateInfo = DateInfo(
                                date = "2025-09-17",
                                weekNumber = "第2周",
                                dayOfWeek = "星期三"
                            )

                            val todayPreviewCourses = listOf(
                                Course(
                                    name = "计算机网络",
                                    time = "1,2",
                                    location = "教10-101",
                                    teacher = "张老师",
                                    duration = "1-16",
                                    dayOfWeek = "2"
                                ),
                                Course(
                                    name = "操作系统",
                                    time = "3,4",
                                    location = "实验楼203",
                                    teacher = "李老师",
                                    duration = "1-16",
                                    dayOfWeek = "2"
                                )
                            )

                            val tomorrowPreviewCourses = listOf(
                                Course(
                                    name = "数据结构",
                                    time = "5,6",
                                    location = "教9-302",
                                    teacher = "王老师",
                                    duration = "1-16",
                                    dayOfWeek = "2"
                                )
                            )

                            val previewState = CourseWidgetState(
                                today = CourseUiState.Success(
                                    SchedulePage(
                                        dateInfo = previewDateInfo,
                                        courses = todayPreviewCourses
                                    )
                                ),
                                tomorrow = CourseUiState.Success(
                                    SchedulePage(
                                        dateInfo = previewDateInfo,
                                        courses = tomorrowPreviewCourses
                                    )
                                )
                            )

                            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                                receiver = CourseWidgetReceiver::class.java,
                                preview = CourseGlanceWidget(),
                                previewState = previewState
                            )
                        }
                    } catch (e: Exception){
                        Toast.makeText(context, "添加失败: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .scrollEndHaptic()
            .overScrollVertical()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .height(windowSize.height.dp),
        overscrollEffect = null
    ) {
        item {
            SmallTitle(text = "配置")
            Card(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                SuperArrow(
                    title = "weiXinID设置",
                    summary = "华交教务weiXinID",
                    onClick = {
                        showWeiXinIdDialog.value = true
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_id_24dp),
                            contentDescription = "微信ID",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperArrow(
                    title = "桌面小组件",
                    onClick =  {
                        onEvent(SettingsEvent.RequestPinAppWidgetClick)
                    },
                    summary = "添加日历小组件到桌面",
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_pin_appwidget),
                            contentDescription = "日历小组件",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperSwitch(
                    checked = uiState.isAutoUpdateCheckEnabled,
                    onCheckedChange = {viewModel.onEvent(SettingsEvent.OnAutoUpdateCheckChanged(!uiState.isAutoUpdateCheckEnabled)) },
                    title = "自动检查更新",
                    summary = "应用启动时检查新版本",
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = "自动检查更新",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperArrow(
                    title = "检查更新",
                    summary = "立即检查是否有新版本",
                    onClick = {
                        Toast.makeText(context, "正在检查更新...", Toast.LENGTH_SHORT).show()
                        viewModel.onEvent(SettingsEvent.OnCheckUpdateNowClick)
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = "检查更新",
                            tint = colorScheme.onBackground
                        )
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            SmallTitle(text = "功能")
            Card(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                SuperArrow(
                    title = "教学校历",
                    summary = "查看本学年教学校历",
                    onClick = {
                        navigator.navigate(AcademicCalendarScreenDestination)
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_date_24dp),
                            contentDescription = "校历",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperArrow(
                    title = "清理缓存",
                    summary = "清理应用缓存",
                    onClick = {
                        showClearCacheDialog.value = true
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_clear_cache),
                            contentDescription = "清理缓存",
                            tint = colorScheme.onBackground
                        )
                    },
                    rightText = uiState.cacheSize
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            SmallTitle(text = "教程")
            Card(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                SuperArrow(
                    title = "使用教程",
                    summary = "查看使用教程",
                    onClick = {
                        openUrl(context, "https://github.com/Replica0110/ECJTU-Calendar/blob/main/README.md")
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_tutorial_24dp),
                            contentDescription = "使用教程",
                            tint = colorScheme.onBackground
                        )
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        item {
            SmallTitle(text = "关于")
            Card(
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                BasicComponent(
                    title = "应用版本",
                    summary = "版本: ${BuildConfig.VERSION_NAME}\n编译时间: ${BuildConfig.BUILD_TIME}",
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_version_24dp),
                            contentDescription = "应用版本",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperArrow(
                    title = "开发者",
                    summary = "可燃乌龙茶",
                    onClick = {
                        openUrl(context, "https://github.com/Replica0110")
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_developer_24dp),
                            contentDescription = "开发者",
                            tint = colorScheme.onBackground
                        )
                    }
                )
                SuperArrow(
                    title = "Github",
                    summary = "项目地址",
                    onClick = {
                        openUrl(context, "https://github.com/Replica0110/ECJTU-Calendar")
                    },
                    leftAction = {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            painter = painterResource(R.drawable.ic_github_24dp),
                            contentDescription = "Github",
                            tint = colorScheme.onBackground
                        )
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}