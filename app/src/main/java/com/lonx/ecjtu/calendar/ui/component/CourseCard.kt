package com.lonx.ecjtu.calendar.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.moriafly.salt.ui.SaltTheme

@Composable
fun CourseCard(course: Course, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SaltTheme.colors.subBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SaltTheme.colors.highlight.copy(alpha = 0.1f, blue = 1f),
                ) {
                    Text(
                        text = "节次：${course.time}",
                        color = SaltTheme.colors.highlight.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 课程名称
                Text(
                    text = course.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // --- 详情部分 ---
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CourseDetailRow(
                    icon = Icons.Outlined.LocationOn,
                    text = "地点：${course.location.ifBlank { "N/A" }}"
                )
                CourseDetailRow(
                    icon = Icons.Outlined.Person,
                    text = "教师：${course.teacher.ifBlank { "N/A" }}"
                )
                CourseDetailRow(
                    icon = Icons.Outlined.DateRange,
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
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorative icon
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant // 相当于 secondary_text
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// --- 预览功能，方便调试 ---
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
                )
            )
        }
    }
}