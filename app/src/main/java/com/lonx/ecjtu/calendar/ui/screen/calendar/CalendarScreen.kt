package com.lonx.ecjtu.calendar.ui.screen.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.lonx.ecjtu.calendar.domain.model.Course
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.ui.component.CourseCard
import com.lonx.ecjtu.calendar.ui.component.DateHeaderCard
import org.koin.androidx.compose.koinViewModel
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.ui.component.CourseDetailBottomSheet
import com.lonx.ecjtu.calendar.ui.component.CustomDatePickerDialog
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.lonx.ecjtu.calendar.domain.model.CalendarError
import com.lonx.ecjtu.calendar.ui.viewmodels.CalendarViewModel
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onNavigateToSettings: () -> Unit) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isViewingToday = uiState.selectedDate == LocalDate.now()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCourse: Course? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "回到今天" 按钮
                if (!isViewingToday) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "已返回今天", Toast.LENGTH_SHORT).show()
                            viewModel.onEvent(CalendarEvent.GoToTodayAndRefresh)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "回到今天")
                    }
                }

                // "选择日期" 按钮，功能和DateHeaderCard重复了，移除
//                IconButton(
//                    onClick = {
//                        showDatePicker = true
//                    }
//                ) {
//                    Icon(painterResource(R.drawable.ic_date_24dp), contentDescription = "选择日期")
//                }

                // "设置" 按钮
                IconButton(onClick = onNavigateToSettings) {
                    Icon(painterResource(R.drawable.ic_settings_24dp), contentDescription = "设置")
                }
            }
        }
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.onEvent(CalendarEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
        )
        {
            // 当内容为空或者有错误时，显示居中的状态信息
            if (uiState.error != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val haptic = LocalHapticFeedback.current
                        DateHeaderCard(
                            dateInfo = uiState.dateInfo,
                            onClick = { showDatePicker = true },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, "已返回当天", Toast.LENGTH_SHORT).show()
                                viewModel.onEvent(CalendarEvent.GoToTodayAndRefresh)
                            }
                        )
                        ErrorState(
                            error = uiState.error!!,
                            onRetry = { viewModel.onEvent(CalendarEvent.Refresh) },
                            onBackToday = {
                                Toast.makeText(context, "已返回当天", Toast.LENGTH_SHORT).show()
                                viewModel.onEvent(CalendarEvent.GoToTodayAndRefresh)
                            },
                            onOpenSettings = onNavigateToSettings
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val haptic = LocalHapticFeedback.current
                        DateHeaderCard(
                            dateInfo = uiState.dateInfo,
                            onClick = { showDatePicker = true },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Toast.makeText(context, "已返回当天", Toast.LENGTH_SHORT).show()
                                viewModel.onEvent(CalendarEvent.GoToTodayAndRefresh)
                            }
                        )
                    }

                    if (uiState.courses.isEmpty() && !uiState.isLoading) {
                        item { EmptyState(message = "今天没有课程安排哦！") }
                    } else {
                        items(uiState.courses) { course ->
                            CourseCard(
                                course = course,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                onClick = {
                                    selectedCourse = course
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    // 显示课程详情底部表
    selectedCourse?.let { course ->
        CourseDetailBottomSheet(
            course = course,
            onDismiss = { selectedCourse = null }
        )
    }

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
private fun ErrorState(error: CalendarError, onRetry: () -> Unit, onBackToday: () -> Unit, onOpenSettings: () -> Unit) {
    RoundedColumn(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            text = error.message?: "未知错误",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (error) {
                is CalendarError.NoWeiXinId, is CalendarError.WeiXinIdInvalid -> {
                    Button(
                        onClick = onRetry,
                        text = "重试",
                        modifier = Modifier.weight(1f)
                        )
                    Button(
                        onClick = onOpenSettings,
                        text = "前往设置",
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    Button(
                        onClick = onRetry,
                        text = "重试",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onBackToday,
                        text = "回到今天",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

    }
}

@Composable
private fun EmptyState(message: String) {
    RoundedColumn(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}


