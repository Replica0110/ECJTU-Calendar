package com.lonx.ecjtu.calendar.ui.screen.calendar

import android.content.ClipData
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.ui.component.CourseCard
import com.lonx.ecjtu.calendar.ui.component.CustomDatePickerDialog
import com.lonx.ecjtu.calendar.ui.component.DateHeaderCard
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.viewmodel.CalendarViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ContactsCircle
import top.yukonga.miuix.kmp.icon.extended.Location
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.WorldClock
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.time.LocalDate


@Composable
@Destination<RootGraph>(label = "日历")
fun CalendarScreen(
    topAppBarScrollBehavior: ScrollBehavior
) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val isViewingToday = uiState.selectedDate == LocalDate.now()

    val showBottomSheet = remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCourse: Course? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        DateHeaderCard(
            dateInfo = uiState.dateInfo,
            onClick = {
                showDatePicker = true
            },
            onLongClick = {
                viewModel.onEvent(CalendarEvent.OnDateChange(LocalDate.now()))
            }
        )
        LazyColumn(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .fillMaxHeight()
                .weight(1f),
            overscrollEffect = null
        ) {
            if (uiState.courses.isNotEmpty()) {
                items(uiState.courses.size) { index ->
                    val course = uiState.courses[index]

                    CourseCard(
                        course = course,
                        onClick = {
                            selectedCourse = course
                            showBottomSheet.value = true
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (uiState.courses.isEmpty() && uiState.error == null && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MessageCard(
                            message = "今天没有课啦~",
                            type = MessageType.Info,
                            onClick = {
                                if (!isViewingToday) {
                                    viewModel.onEvent(CalendarEvent.GoToTodayAndRefresh)
                                }
                            }
                        )
                    }
                }
            }
            item {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error?.let { errorMessage ->
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MessageCard(
                            message = errorMessage,
                            type = MessageType.Warning,
                            onClick = {
                                viewModel.onEvent(CalendarEvent.Refresh)
                            }
                        )
                    }
                }
            }
        }

    }
    SuperBottomSheet(
        onDismissRequest = {
            showBottomSheet.value = false
        },
        show = showBottomSheet,
        title = selectedCourse?.name,
        content = {
            // 教师
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = Color.Transparent),
                onLongPress = {
                    selectedCourse?.teacher?.let { text ->
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("text", text)
                            clipboard.setClipEntry(clipData.toClipEntry())
                        }
                    }
                }
            ) {
                BasicComponent(
                    title = "教师",
                    summary = selectedCourse?.teacher,
                    startAction = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = MiuixIcons.Regular.ContactsCircle,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSecondaryVariant
                            )
                        }
                    }
                )
            }
            // 地点
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = Color.Transparent),
                onLongPress = {
                    selectedCourse?.location?.let { text ->
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("text", text)
                            clipboard.setClipEntry(clipData.toClipEntry())
                        }
                    }
                }
            ) {
                BasicComponent(
                    title = "地点",
                    summary = selectedCourse?.location,
                    startAction = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = MiuixIcons.Regular.Location,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSecondaryVariant
                            )
                        }
                    }
                )
            }
            // 节次
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = Color.Transparent),
                onLongPress = {
                    selectedCourse?.time?.let { text ->
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("text", text)
                            clipboard.setClipEntry(clipData.toClipEntry())
                        }
                    }
                }
            ) {
                BasicComponent(
                    title = "节次",
                    summary = selectedCourse?.time,
                    startAction = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = MiuixIcons.Regular.WorldClock,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSecondaryVariant
                            )
                        }
                    }
                )
            }
            // 上课周
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.defaultColors(color = Color.Transparent),
                onLongPress = {
                    selectedCourse?.duration?.let { text ->
                        coroutineScope.launch {
                            val clipData = ClipData.newPlainText("text", text)
                            clipboard.setClipEntry(clipData.toClipEntry())
                        }
                    }
                }
            ) {
                BasicComponent(
                    title = "上课周",
                    summary = selectedCourse?.duration,
                    startAction = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            Icon(
                                imageVector = MiuixIcons.Regular.Months,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSecondaryVariant
                            )
                        }
                    }
                )
            }
        }
    )



    if (showDatePicker) {
        CustomDatePickerDialog(
            initialDate = viewModel.uiState.collectAsState().value.selectedDate,
            onDateSelected = {
                viewModel.onEvent(CalendarEvent.OnDateChange(it))
                showDatePicker = false
            },
            onDismissRequest = {
                showDatePicker = false
            }
        )
    }

}


