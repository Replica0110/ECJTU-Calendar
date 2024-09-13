package com.lonx.ecjtu.hjcalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.TodayRemoteViewsService
import com.lonx.ecjtu.hjcalendar.service.TomorrowRemoteViewsService
import com.lonx.ecjtu.hjcalendar.utils.CourseData
import com.lonx.ecjtu.hjcalendar.utils.ECJTUCalendarAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        if (shouldUpdateWidget(intent.action)) {
            if (intent.action == Intent.ACTION_TIME_CHANGED) {
                handleTimeChange(context)
            }

            updateWidgets(context)
        }
    }

    private fun shouldUpdateWidget(action: String?): Boolean {
        return action in listOf(
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_TICK,
            "android.appwidget.action.APPWIDGET_UPDATE"
        )
    }

    private fun handleTimeChange(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
        Log.e("onReceive", "workManager已停止")
        startWidgetUpdateWorker(context)
        Log.e("onReceive", "workManager已重启")
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
        startWidgetUpdateWorker(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.e("onDisabled", "onDisabled")
        WorkManager.getInstance(context).cancelAllWork()
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("onUpdate", "onUpdate")
        Log.e("appWidgetIds", appWidgetIds.joinToString())
        val today = getDate()
        val tomorrow = getDate(true)
        val weiXinID = PreferenceManager.getDefaultSharedPreferences(context).getString("weixin_id", "") ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todayCourses = ECJTUCalendarAPI.getCourseHtml(weiXinID, today)?.let { ECJTUCalendarAPI.parseCourseHtml(it) } ?: CourseData.DayCourses()
                val tomorrowCourses = ECJTUCalendarAPI.getCourseHtml(weiXinID, tomorrow)?.let { ECJTUCalendarAPI.parseCourseHtml(it) } ?: CourseData.DayCourses()
                withContext(Dispatchers.Main) {
                    appWidgetIds.forEach { appWidgetId ->
                        updateAppWidget(context, appWidgetManager, appWidgetId, todayCourses, tomorrowCourses)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startWidgetUpdateWorker(context: Context) {
        val periodicUpdateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "widget_course_update",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicUpdateRequest
        )
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        todayCourses: CourseData.DayCourses,
        tomorrowCourses: CourseData.DayCourses
    ) {
        val randomNumber = System.currentTimeMillis() // Use a unique value for each update

        val todayIntent = Intent(context, TodayRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(todayCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("random", randomNumber) // Adding random number to the intent
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
        }

        val tomorrowIntent = Intent(context, TomorrowRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(tomorrowCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("random", randomNumber) // Adding random number to the intent
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
        }

        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            setRemoteAdapter(R.id.lv_course_today, todayIntent)
            setRemoteAdapter(R.id.lv_course_next_day, tomorrowIntent)
            setEmptyView(R.id.lv_course_today, R.id.empty_today)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)
            val (date, weekDay) = getCurrentDateWeekDayWeekNumber()
            setTextViewText(R.id.tv_date, date)
            setTextViewText(R.id.tv_week, weekDay)

            val refreshIntent = Intent(context, CourseWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                putExtra("random", randomNumber) // Adding random number to the intent
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.refresh_button, pendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


    private fun getCurrentDateWeekDayWeekNumber(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M.d", Locale.getDefault())
        val weekDayFormat = SimpleDateFormat("E", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val weekDay = weekDayFormat.format(calendar.time)
        return Pair(date, weekDay)
    }

    private fun getDate(tomorrow: Boolean = false): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        if (tomorrow) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dateFormat.format(calendar.time)
    }
}
