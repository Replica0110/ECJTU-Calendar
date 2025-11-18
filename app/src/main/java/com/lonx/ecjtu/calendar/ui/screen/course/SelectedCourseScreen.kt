package com.lonx.ecjtu.calendar.ui.screen.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.domain.model.SelectedCourse
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.component.SurfaceTag
import com.lonx.ecjtu.calendar.ui.viewmodel.SelectedCourseViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
@Destination<RootGraph>(label = "已选课程")
fun SelectedCourseScreen(
    topAppBarScrollBehavior: ScrollBehavior
) {
    val viewModel: SelectedCourseViewModel = koinViewModel()
    val windowSize = getWindowSize()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.courses) {
        if (uiState.courses.isEmpty() && uiState.error == null && !uiState.isLoading) {
            viewModel.loadCourses()
        }
    }
    Column(
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    ) {
        uiState.error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                MessageCard(
                    message = errorMessage,
                    type = MessageType.Warning,
                    onClick = {
                        // 点击错误消息时执行手动刷新（从网络抓取并保存到数据库）
                        viewModel.loadCourses(refresh = true)
                    }
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .height(windowSize.height.dp)
                .weight(1f),
            overscrollEffect = null
        ) {
            item {
                if (!uiState.isLoading && uiState.error == null) {
                    // 显示上次刷新时间
                    val lastRefreshText = if (uiState.lastRefreshMillis > 0L) {
                        android.text.format.DateUtils.getRelativeTimeSpanString(
                            uiState.lastRefreshMillis,
                            System.currentTimeMillis(),
                            android.text.format.DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                    } else {
                        "未从教务获取"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "更新于：$lastRefreshText",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onBackgroundVariant
                        )
                    }

                    uiState.courses.forEach {
                        CourseCard(course = it, term = uiState.currentTerm)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: SelectedCourse,
    term: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* -------------------- 标题 & 学期 -------------------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.courseName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Text(
                    text = term,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SurfaceTag(text = "教师: ${course.courseTeacher}")
                SurfaceTag(text = "考核方式: ${course.checkType}")
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "教学班名称",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onBackgroundVariant
                )
                Text(
                    text = course.className,
                    style = MiuixTheme.textStyles.footnote1
                )
            }
            if (course.classTime.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "上课时间",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onBackgroundVariant
                    )
                    Text(
                        text = course.classTime.replace("|", "\n").removeSuffix("\n"),
                        style = MiuixTheme.textStyles.footnote1
                    )
                }

            }
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    InfoItem(title = "课程要求", value = course.courseRequire)

                    InfoItem(title = "选课类型", value = course.courseType)
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    InfoItem(title = "学时", value = "${course.period}")
                    InfoItem(title = "学分", value = "${course.credit}")
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {

                    InfoItem(title = "选课模块", value = course.selectedType)
                    InfoItem(title = "选课状态", value = course.isSelected)
                }
            }

        }
    }
}

@Composable
private fun InfoItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onBackgroundVariant
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.footnote1,
            fontWeight = FontWeight.Medium
        )
    }
}


@Preview
@Composable
fun CourseCardPreview() {
    CourseCard(
        course = SelectedCourse(
            courseName = "计算机组成原理",
            courseTeacher = "王伟",
            classTime = "第2-18周 星期三 第1,2节(单)[10-111]|第2-18周 星期一 第1,2节[10-103]|",
            courseType = "必修",
            checkType = "考查",
            selectedType = "计划内选课",
            period = 32.0,
            credit = 2.0,
            isSelected = "已选",
            className = "计算机组成原理(20231-1)",
            courseRequire = "必修课"
        ),
        term = "2023.1"
    )
}