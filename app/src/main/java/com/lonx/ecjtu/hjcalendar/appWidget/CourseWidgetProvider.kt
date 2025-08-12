package com.lonx.ecjtu.hjcalendar.appWidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.MainActivity
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.CourseRemoteViewsService
import com.lonx.ecjtu.hjcalendar.utils.CourseData
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.lonx.ecjtu.hjcalendar.logic.DataStoreManager

const val ACTION_MANUAL_REFRESH = "com.lonx.ecjtu.hjcalendar.widget.MANUAL_REFRESH"

class CourseWidgetProvider : AppWidgetProvider() {
    private var lastUpdateTime = 0L
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let { Log.e("intent.action", it) }
        if (shouldUpdateWidget(intent.action)) {
            updateWidgets(context)
        }
    }

    private fun shouldUpdateWidget(action: String?): Boolean {
        return action in listOf(
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            "android.appwidget.action.APPWIDGET_UPDATE"
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
        if (appWidgetIds.isEmpty()) {
            return
        }
        val todayStr = getFormatDate()
        val tomorrowStr = getFormatDate(true)

        val weiXinID = DataStoreManager.getWeiXinId()

        CoroutineScope(Dispatchers.IO).launch {
            var todayCourses: CourseData.DayCourses
            var tomorrowCourses: CourseData.DayCourses
            try {
                val rawTodayCourses = ECJTUCalendarAPI.getCourseHtml(weiXinID, todayStr)?.let { ECJTUCalendarAPI.parseCourseHtml(it) }
                val rawTomorrowCourses = ECJTUCalendarAPI.getCourseHtml(weiXinID, tomorrowStr)?.let { ECJTUCalendarAPI.parseCourseHtml(it) }

                todayCourses = processCourseData(context, rawTodayCourses, todayStr)
                tomorrowCourses = processCourseData(context, rawTomorrowCourses, tomorrowStr)
            } catch (e: Exception) {
                e.printStackTrace()
                todayCourses = CourseData.DayCourses(todayStr, emptyList())
                tomorrowCourses = CourseData.DayCourses(tomorrowStr, emptyList())
            }

            withContext(Dispatchers.Main) {
                appWidgetIds.forEach { appWidgetId ->
                    updateAppWidget(context, appWidgetManager, appWidgetId, todayCourses, tomorrowCourses)
                }
            }
        }
    }
    /**
     * 用于处理空/错误课程数据
     */
    private fun processCourseData(context: Context, dayCourses: CourseData.DayCourses?, dateStr: String): CourseData.DayCourses {
        if (dayCourses != null) {
            if (dayCourses.courses.isEmpty()) {
                val defaultText = context.getString(R.string.empty_course)
                val customText = DataStoreManager.getNoCourseText(defaultText)
                val emptyInfoCourse = CourseData.CourseInfo(courseName = "课表为空", courseLocation = customText)
                return dayCourses.copy(courses = listOf(emptyInfoCourse))
            }
            return dayCourses
        }

        else {
            // 获取当天的星期信息
            val calendar = Calendar.getInstance()
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = inputFormat.parse(dateStr) ?: Date()
            val weekDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)

            // 构造一个用于显示的错误日期字符串
            val errorDateString = "$dateStr $weekDay (网络错误)"
            val errorCourse = CourseData.CourseInfo(courseName = "课表加载错误", courseLocation = "请检查网络或配置")

            return CourseData.DayCourses(errorDateString, listOf(errorCourse))
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todayCourses: CourseData.DayCourses,
        tomorrowCourses: CourseData.DayCourses
    ) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < 1000) { // 小于1秒的更新直接跳过
            Log.i("日历小组件更新防抖","跳过重复更新")
            return
        }
        lastUpdateTime = now
        val randomNumber = System.currentTimeMillis() // Use a unique value for each update
        val itemClickIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.lonx.ecjtu.pda.action.VIEW_COURSE_DETAIL"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://item_click_template/$appWidgetId".toUri()
        }

        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val itemClickPendingIntentTemplate = PendingIntent.getActivity(
            context,
            appWidgetId,
            itemClickIntent,
            flags
        )
        val intentToday = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(todayCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://${context.packageName}/$appWidgetId/today/$randomNumber".toUri()
        }

        val intentTomorrow = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(tomorrowCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = "widget://${context.packageName}/$appWidgetId/tomorrow/$randomNumber".toUri()
        }
        // 点击刷新按钮
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
        val (date, weekDay, weekNumber) = getToday(todayCourses.date)
        val todayEmptyText = todayCourses.courses.firstOrNull()?.courseLocation ?: context.getString(R.string.empty_course)
        val tomorrowEmptyText = tomorrowCourses.courses.firstOrNull()?.courseLocation ?: context.getString(R.string.empty_course)
        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            // 更新标题栏，这一步现在总是能获取到有效数据
            setTextViewText(R.id.tv_date, date)
            setTextViewText(R.id.tv_week, weekDay)
            setTextViewText(R.id.tv_week_number, weekNumber)

            // 设置 Adapter
            setRemoteAdapter(R.id.lv_course_today, intentToday)
            setRemoteAdapter(R.id.lv_course_next_day, intentTomorrow)

            setEmptyView(R.id.lv_course_today, R.id.empty_today)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)

            setTextViewText(R.id.empty_today, todayEmptyText)
            setTextViewText(R.id.empty_next_day, tomorrowEmptyText)

            // 设置点击模板和刷新按钮
            setPendingIntentTemplate(R.id.lv_course_today, itemClickPendingIntentTemplate)
            setPendingIntentTemplate(R.id.lv_course_next_day, itemClickPendingIntentTemplate)
            setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)
        }
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
