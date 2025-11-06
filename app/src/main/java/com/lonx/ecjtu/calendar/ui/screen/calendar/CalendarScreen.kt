package com.lonx.ecjtu.calendar.ui.screen.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.lonx.ecjtu.calendar.domain.model.Course
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.lonx.ecjtu.calendar.ui.component.CustomDatePickerDialog
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.lonx.ecjtu.calendar.domain.error.CalendarError
import com.lonx.ecjtu.calendar.ui.component.CourseCard
import com.lonx.ecjtu.calendar.ui.component.DateHeaderCard
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.viewmodel.CalendarViewModel
import com.moriafly.salt.ui.ItemButton
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.extra.SuperBottomSheet
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic


@Composable
@Destination<RootGraph>(label = "日历")
fun CalendarScreen(
    topAppBarScrollBehavior: ScrollBehavior
) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isViewingToday = uiState.selectedDate == LocalDate.now()

    val showBottomSheet = remember { mutableStateOf(false) }
    val windowSize = getWindowSize()
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
                .height(windowSize.height.dp)
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
                            .fillParentMaxWidth()
                            .padding(12.dp),
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
                            .fillParentMaxWidth()
                            .padding(12.dp),
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
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicComponent(
                    title = "教师",
                    summary = selectedCourse?.teacher
                )
                BasicComponent(
                    title = "地点",
                    summary = selectedCourse?.location
                )
                BasicComponent(
                    title = "节次",
                    summary = selectedCourse?.time
                )
                BasicComponent(
                    title = "上课周",
                    summary = selectedCourse?.duration
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

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun ErrorState(error: CalendarError, onRetry: () -> Unit, onBackToday: () -> Unit) {
    RoundedColumn(Modifier.fillMaxWidth()) {
        ItemInfo(
            text = error.message ?: "未知错误",
            infoType = ItemInfoType.Error
        )
        when (error) {
            is CalendarError.NoWeiXinId, is CalendarError.WeiXinIdInvalid -> {
                ItemButton(
                    onClick = onRetry,
                    text = "重试"
                )
            }

            else -> {
                ItemButton(
                    onClick = onRetry,
                    text = "重试"
                )
                ItemButton(
                    onClick = onBackToday,
                    text = "回到今天"
                )
            }
        }


    }
}

@OptIn(UnstableSaltUiApi::class)
@Composable
private fun EmptyState(message: String) {
    RoundedColumn(Modifier.fillMaxWidth()) {
        ItemInfo(
            text = message,
            infoType = ItemInfoType.Success
        )
    }
}


