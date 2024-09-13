package com.lonx.ecjtu.hjcalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.TodayRemoteViewsService
import com.lonx.ecjtu.hjcalendar.service.TomorrowRemoteViewsService
import com.lonx.ecjtu.hjcalendar.util.CourseData
import com.lonx.ecjtu.hjcalendar.util.ECJTUCalendarAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class CourseWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        intent.action?.let { Log.e("intent.action", it) }
        if (intent.action==Intent.ACTION_TIME_CHANGED||
            intent.action== Intent.ACTION_TIMEZONE_CHANGED||
            intent.action == Intent.ACTION_DATE_CHANGED||
            intent.action == Intent.ACTION_TIME_TICK||
            intent.action=="android.appwidget.action.APPWIDGET_UPDATE") {

            // 如果是手动调整时间，重新启动workManager
            if (intent.action == Intent.ACTION_TIME_CHANGED) {
                WorkManager.getInstance(context).cancelAllWork()
                Log.e("onReceive", "workManager已停止")
                startWidgetUpdateWorker(context)
                Log.e("onReceive", "workManager已重启")
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CourseWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.e("onEnabled", "onEnabled")
        startWidgetUpdateWorker(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // 停止 WorkManager 调度
        Log.e("onDisabled", "onDisabled")
        WorkManager.getInstance(context).cancelAllWork()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("onUpdate", "onUpdate")

        val today=getDate()
        val tomorrow=getDate(true)
        val weiXinID=PreferenceManager.getDefaultSharedPreferences(context).getString("weixin_id", "")?:""
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val todayHtmlInfo=ECJTUCalendarAPI.getCourseHtml(weiXinID, today)?:""
                val todayCourses= ECJTUCalendarAPI.parseCourseHtml(todayHtmlInfo)
                Log.e("todayCourses", todayCourses.toString())
                val tomorrowHtmlInfo=ECJTUCalendarAPI.getCourseHtml(weiXinID, tomorrow)?:""
                val tomorrowCourses= ECJTUCalendarAPI.parseCourseHtml(tomorrowHtmlInfo)
                delay(1000)
                withContext(Dispatchers.Main){
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId,todayCourses, tomorrowCourses)
                        // 设置点击更新事件
                        val intent = Intent(context, CourseWidgetProvider::class.java).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        val views = RemoteViews(context.packageName, R.layout.widget_course)
                        views.setOnClickPendingIntent(R.id.refresh_button, pendingIntent) // 点击按钮触发更新

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun startWidgetUpdateWorker(context: Context) {

        val periodicUpdateRequest =
            PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            ).build()
        //添加UniqueWork，不用怕会重复添加
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "widget_course_update",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicUpdateRequest
            )

    }
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todayCourses: CourseData.DayCourses,
        tomorrowCourses: CourseData.DayCourses
    ) {
        // 从 SharedPreferences 中获取 weiXinID
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val weiXinID = preferences.getString("weixin_id", "")
        Log.e("onUpdate", "weiXinID: $weiXinID")
        // 构建 TodayRemoteViewsService 的 Intent
        val todayIntent = Intent(context, TodayRemoteViewsService::class.java).apply {
            putExtra("weiXinID", weiXinID)
            putExtra("dayCourses", Gson().toJson(todayCourses))
        }
        // 构建 TomorrowRemoteViewsService 的 Intent
        val tomorrowIntent = Intent(context, TomorrowRemoteViewsService::class.java).apply {
            putExtra("weiXinID", weiXinID)
            putExtra("dayCourses", Gson().toJson(tomorrowCourses))
        }


        // 创建 RemoteViews，绑定到小组件布局
        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            setRemoteAdapter(R.id.lv_course_today, todayIntent)
            setRemoteAdapter(R.id.lv_course_next_day, tomorrowIntent)
            setEmptyView(R.id.lv_course_today, R.id.empty_today)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)
        }
        // 设置小组件的 ID
        todayIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        tomorrowIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // 设置远程适配器
        val (date, weekDay) = getCurrentDateWeekDayWeekNumber() // 获取当前日期和星期以更新小组件显示的时间
        views.setTextViewText(R.id.tv_date, date)
        views.setTextViewText(R.id.tv_week, weekDay)
        Log.e("onUpdate", "appWidgetId: $appWidgetId")
        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getCurrentDateWeekDayWeekNumber(): Pair<String, String> { // 获取当前日期和星期以更新父视图
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M.d", Locale.getDefault())
        val weekDayFormat = SimpleDateFormat("E", Locale.getDefault())

        val date = dateFormat.format(calendar.time)
        val weekDay = weekDayFormat.format(calendar.time)

        return Pair(date, weekDay)
    }
    private fun getDate(tomorrow: Boolean=false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (tomorrow){
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dateFormat.format(calendar.time)
    }
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
}
