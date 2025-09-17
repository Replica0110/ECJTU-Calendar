package com.lonx.ecjtu.calendar.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.domain.model.Course
import com.lonx.ecjtu.calendar.domain.model.DateInfo
import com.moriafly.salt.ui.SaltTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CourseGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = CourseWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 在小组件首次创建时，触发一次数据加载
        UpdateAction().onAction(
            context = context,
            glanceId = id,
            parameters = actionParametersOf()
        )

        provideContent {
            val state = currentState<CourseWidgetState>()
            WidgetContent(state = state)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(state: CourseWidgetState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
                .padding(8.dp)
                .cornerRadius(16.dp)
        ) {
            // 标题栏
            TitleBar(dateInfo = (state.today as? CourseUiState.Success)?.page?.dateInfo)

            // "今天" "明天" 标题
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "今天",
                    modifier = GlanceModifier.defaultWeight().padding(start = 6.dp),
                    style = TextStyle(color = ColorProvider(R.color.gray), fontSize = 12.sp)
                )
                Text(
                    text = "明天",
                    modifier = GlanceModifier.defaultWeight().padding(start = 14.dp),
                    style = TextStyle(color = ColorProvider(R.color.gray), fontSize = 12.sp)
                )
            }

            // 课程列表
            Spacer(modifier = GlanceModifier.height(4.dp))
            Row(modifier = GlanceModifier.fillMaxSize()) {
                CourseList(state = state.today, modifier = GlanceModifier.defaultWeight())
                Spacer(modifier = GlanceModifier.width(8.dp))
                Image(
                    provider = ImageProvider(R.drawable.widget_divider_vertical),
                    contentDescription = "divider",
                    modifier = GlanceModifier.fillMaxHeight().width(1.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                CourseList(state = state.tomorrow, modifier = GlanceModifier.defaultWeight())
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun TitleBar(dateInfo: DateInfo?) {
    var dateText: String
    val weekNumberText: String
    val dayOfWeekText: String

    if (dateInfo != null) {
        try {
            val date = LocalDate.parse(dateInfo.date, DateTimeFormatter.ISO_LOCAL_DATE)
            dateText = date.format(DateTimeFormatter.ofPattern("M.d"))
        } catch (e: Exception) {
            dateText = dateInfo.date
        }
        dayOfWeekText = "第${dateInfo.dayOfWeek}周"
        weekNumberText = dateInfo.weekNumber
    } else {
        val now = LocalDate.now()
        dateText = now.format(DateTimeFormatter.ofPattern("M.d"))
        dayOfWeekText = "未知周"
        weekNumberText = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.CHINA))
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth().height(18.dp).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = LocalContext.current.getString(R.string.app_name),
            style = TextStyle(color = ColorProvider(R.color.primary_lite), fontSize = 13.sp),
            modifier = GlanceModifier.width(55.dp)
        )
        Image(
            provider = ImageProvider(R.drawable.ic_refresh_button),
            contentDescription = "Refresh",
            modifier = GlanceModifier
                .size(18.dp)
                .clickable(actionRunCallback<UpdateAction>())
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = dateText,
            style = TextStyle(color = ColorProvider(R.color.gray), fontSize = 13.sp),
            modifier = GlanceModifier.padding(end = 8.dp)
        )
        Text(
            text = dayOfWeekText,
            style = TextStyle(color = ColorProvider(R.color.gray), fontSize = 13.sp),
            modifier = GlanceModifier.padding(end = 8.dp)
        )
        Text(
            text = weekNumberText,
            style = TextStyle(color = ColorProvider(R.color.gray), fontSize = 13.sp)
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CourseList(state: CourseUiState, modifier: GlanceModifier) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is CourseUiState.Loading -> CircularProgressIndicator()
            is CourseUiState.Error -> Text(text = "加载失败: ${state.message}")
            is CourseUiState.Success -> {
                if (state.page.courses.isEmpty()) {
                    Text(
                        text = "今天没课啦",
                        style = TextStyle(color = ColorProvider(android.R.color.darker_gray))
                    )
                } else {
                    LazyColumn {
                        items(state.page.courses) { course ->
                            CourseItem(course = course)
                            Spacer(modifier = GlanceModifier.height(4.dp)) // 课程间距
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun CourseItem(course: Course) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(SaltTheme.colors.background)
            .padding(horizontal = 1.dp, vertical = 2.dp)
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_divider_primary),
            contentDescription = "divider",
            modifier = GlanceModifier.fillMaxHeight().padding(start = 4.dp)
        )
        Column(modifier = GlanceModifier.padding(start = 6.dp).fillMaxWidth()) {
            Text(
                text = course.name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(R.color.black)
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "节次: ${course.time}",
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(R.color.black)),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "地点: ${course.location}",
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(R.color.black)),
                maxLines = 1
            )
        }
    }
}

