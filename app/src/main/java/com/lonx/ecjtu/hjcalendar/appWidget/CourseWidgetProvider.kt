package com.lonx.ecjtu.hjcalendar.appWidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.MainActivity
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule
import com.lonx.ecjtu.hjcalendar.data.model.ScheduleResult
import com.lonx.ecjtu.hjcalendar.data.repository.CalendarRepository
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager
import com.lonx.ecjtu.hjcalendar.service.CourseRemoteViewsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

const val ACTION_MANUAL_REFRESH = "com.lonx.ecjtu.hjcalendar.widget.MANUAL_REFRESH"

class CourseWidgetProvider : AppWidgetProvider() {
//    companion object {
//        fun updateAllWidgets(context: Context) {
//            val intent = Intent(context, CourseWidgetProvider::class.java).apply {
//                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//            }
//            context.sendBroadcast(intent)
//        }
//    }

    private var lastUpdateTime = 0L
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let { action ->
            Log.e("onReceive", action)
            when (action) {
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    // 系统启动或应用更新后，强制更新所有widget
                    Log.e("Widget", "系统启动或应用更新，正在更新小组件")
                    updateWidgets(context)
                }
                else -> {
                    if (shouldUpdateWidget(action)) {
                        updateWidgets(context)
                    }
                }
            }
        }
    }

    private fun shouldUpdateWidget(action: String?): Boolean {
        return action in listOf(
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.appwidget.action.APPWIDGET_UPDATE",
            ACTION_MANUAL_REFRESH
        )
    }

    private fun updateWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, CourseWidgetProvider::class.java)
        )
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.e("onEnabled", "onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.e("onDisabled", "onDisabled")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return
        val repository = CalendarRepository()
        val todayStr = getFormatDate()
        val tomorrowStr = getFormatDate(true)
        val weiXinID = DataStoreManager.getWeiXinId()

        CoroutineScope(Dispatchers.IO).launch {
            val todayResult = repository.getDailyCourses(weiXinID, todayStr)
            val tomorrowResult = repository.getDailyCourses(weiXinID, tomorrowStr)

            val todaySchedule = processScheduleResult(context, todayResult, todayStr)
            val tomorrowSchedule = processScheduleResult(context, tomorrowResult, tomorrowStr)

            withContext(Dispatchers.Main) {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId, todaySchedule, tomorrowSchedule)
                }
            }
        }
    }

    /**
     * 处理课程数据结果
     */
    private fun processScheduleResult(
        context: Context,
        result: ScheduleResult,
        dateStr: String
    ): DailySchedule {
        return when (result) {
            is ScheduleResult.Success -> {
                if (result.schedule.courses.isEmpty()) {
                    // 当天无课
                    val defaultText = context.getString(R.string.empty_course)
                    val customText = DataStoreManager.getNoCourseText(defaultText)
                    result.schedule.copy(
                        courses = listOf(
                            Course.createEmpty(customText)
                        )
                    )
                } else {
                    result.schedule
                }
            }
            is ScheduleResult.Empty -> {
                val defaultText = context.getString(R.string.empty_course)
                val customText = DataStoreManager.getNoCourseText(defaultText)
                DailySchedule(
                    dateInfo = result.dateInfo,
                    courses = listOf(
                        Course.createEmpty(customText)
                    )
                )
            }
            is ScheduleResult.Error -> {
                // 构造错误信息的日期标题
                val calendar = Calendar.getInstance()
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                try {
                    calendar.time = inputFormat.parse(dateStr) ?: Date()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val weekDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
                val errorDateString = "$dateStr $weekDay (加载失败)"

                DailySchedule(
                    dateInfo = errorDateString,
                    courses = listOf(
                        Course.createError(result.message)
                    )
                )
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todaySchedule: DailySchedule,
        tomorrowSchedule: DailySchedule
    ) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < 1000) { // 小于1秒的更新直接跳过
            Log.i("日历小组件更新防抖","跳过重复更新")
            return
        }
        lastUpdateTime = now

        val randomNumber = System.currentTimeMillis() // 用于生成唯一的URI

        // 设置点击课程项时的模板Intent
        val itemClickIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.lonx.ecjtu.pda.action.VIEW_COURSE_DETAIL"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://item_click_template/$appWidgetId".toUri()
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val itemClickPendingIntentTemplate = PendingIntent.getActivity(
            context,
            appWidgetId,
            itemClickIntent,
            flags
        )

        // 设置今天的课程列表Intent
        val intentToday = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("schedule", Gson().toJson(todaySchedule))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://${context.packageName}/$appWidgetId/today/$randomNumber".toUri()
        }

        // 设置明天的课程列表Intent
        val intentTomorrow = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("schedule", Gson().toJson(tomorrowSchedule))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://${context.packageName}/$appWidgetId/tomorrow/$randomNumber".toUri()
        }

        // 设置刷新按钮Intent
        val refreshIntent = Intent(context, CourseWidgetProvider::class.java).apply {
            action = ACTION_MANUAL_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://${context.packageName}/$appWidgetId/refresh".toUri()
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 解析日期信息
        val (date, weekDay, weekNumber) = getToday(todaySchedule.dateInfo)

        // 获取空视图的文本
        val todayEmptyText = todaySchedule.courses.firstOrNull()?.msg
            ?: context.getString(R.string.empty_course)
        val tomorrowEmptyText = tomorrowSchedule.courses.firstOrNull()?.msg
            ?: context.getString(R.string.empty_course)

        // 创建和配置RemoteViews
        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            // 更新标题栏
            setTextViewText(R.id.tv_date, date)
            setTextViewText(R.id.tv_week, weekDay)
            setTextViewText(R.id.tv_week_number, weekNumber)

            // 设置适配器
            @Suppress("DEPRECATION")
            setRemoteAdapter(R.id.lv_course_today, intentToday)
            @Suppress("DEPRECATION")
            setRemoteAdapter(R.id.lv_course_next_day, intentTomorrow)

            // 设置空视图
            setEmptyView(R.id.lv_course_today, R.id.empty_today)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)
            setTextViewText(R.id.empty_today, todayEmptyText)
            setTextViewText(R.id.empty_next_day, tomorrowEmptyText)

            // 设置点击事件
            setPendingIntentTemplate(R.id.lv_course_today, itemClickPendingIntentTemplate)
            setPendingIntentTemplate(R.id.lv_course_next_day, itemClickPendingIntentTemplate)
            setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)
        }

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


    private fun getToday(time: String): Triple<String, String, String> {
        val pattern = """(\d{4}-\d{2}-\d{2})\s(星期.+)（(第\d+周)）""".toRegex()

        val matchResult = pattern.find(time)

        if (matchResult != null) {
            val (date, weekDay, weekNumber) = matchResult.destructured
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("M.d", Locale.getDefault())
            val formatDate = inputFormat.parse(date) ?: Date()
            val formattedDate = outputFormat.format(formatDate)
            return Triple(formattedDate, weekDay, weekNumber)
        } else {
            return Triple("11.45", "星期八", "第14周")
        }
    }

    private fun getFormatDate(tomorrow: Boolean = false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (tomorrow) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dateFormat.format(calendar.time)
    }
}
