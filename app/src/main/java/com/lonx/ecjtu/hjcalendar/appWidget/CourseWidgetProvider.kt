package com.lonx.ecjtu.hjcalendar.appWidget

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
        Log.e("onReceive", "workManager stop")
        startWidgetUpdateWorker(context)
        Log.e("onReceive", "workManager restart")
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
        val today = getFormatDate()
        val tomorrow = getFormatDate(true)
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
        val intentToday = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(todayCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("random", randomNumber) // Adding random number to the intent
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
        }

        val intentTomorrow = Intent(context, CourseRemoteViewsService::class.java).apply {
            putExtra("dayCourses", Gson().toJson(tomorrowCourses))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("random", randomNumber) // Adding random number to the intent
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
        }
        val (date, weekDay, weekNumber) = getToday(todayCourses.date)
        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            setRemoteAdapter(R.id.lv_course_today, intentToday)
            setRemoteAdapter(R.id.lv_course_next_day, intentTomorrow)
            setEmptyView(R.id.lv_course_today, R.id.empty_today)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)
            setTextViewText(R.id.tv_date, date)
            setTextViewText(R.id.tv_week, weekDay)
            setTextViewText(R.id.tv_week_number, weekNumber)
            // 点击刷新按钮
            val refreshIntent = Intent(context, CourseWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                putExtra("random", randomNumber) // Adding random number to the intent
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME)) // Make the intent unique
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


    private fun getToday(time:String): Triple<String, String, String> {
        val pattern = """(\d{4}-\d{2}-\d{2})\s(星期.+)（(第\d+周)）""".toRegex()

        val matchResult = pattern.find(time)
        if (matchResult != null) {
            val (date, weekDay, weekNumber) = matchResult.destructured
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("M.d", Locale.getDefault())
            val formatDate = inputFormat.parse(date)?: Date()
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
