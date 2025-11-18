package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        insideMargin = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                SurfaceTag("节次：${course.time}")

                Spacer(modifier = Modifier.width(10.dp))

                // 课程名称
                Text(
                    text = course.name,
                    fontWeight = FontWeight.Bold,
                    style = MiuixTheme.textStyles.title4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CourseDetailRow(
                    icon = painterResource(R.drawable.ic_location),
                    text = "地点：${course.location.ifBlank { "N/A" }}"
                )
                CourseDetailRow(
                    icon = painterResource(R.drawable.ic_teacher),
                    text = "教师：${course.teacher.ifBlank { "N/A" }}"
                )
                CourseDetailRow(
                    icon = painterResource(R.drawable.ic_class_time),
                    text = "上课周：${course.duration.ifBlank { "N/A" }}"
                )
            }
        }
    }
}


/**
 * 一个可复用的私有 Composable，用于显示一行 "图标 + 文本" 的详情。
 * @param icon 左侧显示的图标。
 * @param text 右侧显示的文本。
 * @param modifier Modifier for this composable.
 */
@Composable
private fun CourseDetailRow(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MiuixTheme.colorScheme.onSecondaryVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSecondaryVariant
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CourseCardPreview() {
    CalendarTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CourseCard(
                course = Course(
                    duration = "1-16",
                    time = "3-4节",
                    name = "马克思主义基本原理",
                    location = "14-408",
                    teacher = "温旭琼",
                    dayOfWeek = "星期一"
                ),
                onClick = {
                    // 点击事件的响应逻辑
                }
            )
        }
    }
}