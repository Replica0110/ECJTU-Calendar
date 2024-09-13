package com.lonx.ecjtu.hjcalendar.appWidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lonx.ecjtu.hjcalendar.utils.CourseData
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WidgetUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val (todayCourses, tomorrowCourses) = fetchCourses()
            updateWidgets(todayCourses, tomorrowCourses)
            Result.success()
        } catch (e: Exception) {
            Log.e("WidgetUpdateWorker", "Error updating widgets", e)
            Result.failure()
        }
    }

    private suspend fun fetchCourses(): Pair<CourseData.DayCourses, CourseData.DayCourses> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val weiXinID = preferences.getString("weixin_id", "").orEmpty()
        Log.d("WidgetUpdateWorker", "Fetching courses")

        val todayCourses = fetchCourseData(weiXinID, getDate())
        val tomorrowCourses = fetchCourseData(weiXinID, getDate(true))

        return Pair(todayCourses, tomorrowCourses)
    }

    private suspend fun fetchCourseData(weiXinID: String, date: String): CourseData.DayCourses {
        return try {
            val html = ECJTUCalendarAPI.getCourseHtml(weiXinID, date).orEmpty()
            if (html.isBlank() || html.contains("<title>教务处微信平台绑定</title>")) {
                Log.e("WidgetUpdateWorker", "Invalid or blank HTML response")
                // Handle invalid HTML response
                CourseData.DayCourses(date, listOf(CourseData.CourseInfo(courseName = "课表加载错误")))
            } else {
                ECJTUCalendarAPI.parseCourseHtml(html)
            }
        } catch (e: Exception) {
            Log.e("WidgetUpdateWorker", "Error fetching course data", e)
            // Handle parsing errors
            CourseData.DayCourses(date, listOf(CourseData.CourseInfo(courseName = "课表加载错误")))
        }
    }

    private fun getDate(tomorrow: Boolean = false): String {
        val calendar = Calendar.getInstance()
        if (tomorrow) calendar.add(Calendar.DAY_OF_YEAR, 1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun updateWidgets(todayCourses: CourseData.DayCourses, tomorrowCourses: CourseData.DayCourses) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val thisWidget = ComponentName(appContext, CourseWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            val provider = CourseWidgetProvider()
            for (appWidgetId in appWidgetIds) {
                provider.updateAppWidget(appContext, appWidgetManager, appWidgetId, todayCourses, tomorrowCourses)
            }
            Log.d("WidgetUpdateWorker", "Widgets updated successfully.")
        } catch (e: Exception) {
            Log.e("WidgetUpdateWorker", "Error updating widgets", e)
        }
    }
}

