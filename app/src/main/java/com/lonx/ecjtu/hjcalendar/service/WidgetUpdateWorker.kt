package com.lonx.ecjtu.hjcalendar.service

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lonx.ecjtu.hjcalendar.util.CourseData
import com.lonx.ecjtu.hjcalendar.util.ECJTUCalendarAPI
import com.lonx.ecjtu.hjcalendar.widget.CourseWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WidgetUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {

        return try {
            val (todayCourses, tomorrowCourses) = getTodayAndTomorrowCourse(appContext)
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val thisWidget = ComponentName(appContext, CourseWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val provider = CourseWidgetProvider()
            for (appWidgetId in appWidgetIds){
                provider.updateAppWidget(appContext, appWidgetManager, appWidgetId,todayCourses, tomorrowCourses)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }


    }
    private suspend fun getTodayAndTomorrowCourse(appContext: Context): Pair<CourseData.DayCourses, CourseData.DayCourses> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val weiXinID = preferences.getString("weixin_id", "")?: ""
        Log.e("getTodayAndTomorrowCourse", "getTodayAndTomorrowCourse", )
        val todayHtml = ECJTUCalendarAPI.getCourseHtml(weiXinID, getDate()) ?: ""
        val todayCourses: CourseData.DayCourses = ECJTUCalendarAPI.parseCourseHtml(todayHtml)
        val tomorrowHtml = ECJTUCalendarAPI.getCourseHtml(weiXinID, getDate(true)) ?: ""
        val tomorrowCourses: CourseData.DayCourses = ECJTUCalendarAPI.parseCourseHtml(tomorrowHtml)
        return Pair(todayCourses, tomorrowCourses)
    }
    private fun getDate(tomorrow: Boolean=false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (tomorrow){
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dateFormat.format(calendar.time)
    }
}