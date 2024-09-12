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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lonx.ecjtu.hjcalendar.MainActivity
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.TodayRemoteViewsService
import com.lonx.ecjtu.hjcalendar.service.TomorrowRemoteViewsService
import com.lonx.ecjtu.hjcalendar.util.ECJTUCalendarAPI
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
            intent.action == Intent.ACTION_TIME_TICK) {

            // 如果是手动调整时间，重新启动workManager
            if (intent.action == Intent.ACTION_TIME_CHANGED) {
                WorkManager.getInstance(context).cancelAllWork()
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
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun startWidgetUpdateWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // 创建一个 OneTimeWorkRequest，每 5 秒循环一次
        val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        workManager.enqueue(workRequest)
        Log.e("startWidgetUpdateWorker", "startWidgetUpdateWorker")

        // 监听任务完成，完成后重新执行
        workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
            if (workInfo != null && workInfo.state.isFinished) {
                // 任务完成后，重新调度任务
                startWidgetUpdateWorker(context)
                Log.e("startWidgetUpdateWorker", "startWidgetUpdateWorker")
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        // 从 SharedPreferences 中获取 weiXinID
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val weiXinID = preferences.getString("weixin_id", "")
        Log.e("更新小组件", "weiXinID: $weiXinID")
        // 获取今天和明天的格式化的日期，用于获取课表数据
        val todayIntent = Intent(context, TodayRemoteViewsService::class.java).apply {
            putExtra("weiXinID", weiXinID)
        }
        val tomorrowIntent = Intent(context, TomorrowRemoteViewsService::class.java).apply {
            putExtra("weiXinID", weiXinID)
        }
        // 创建 RemoteViews，绑定到小组件布局
        val views = RemoteViews(context.packageName, R.layout.widget_course).apply {
            setRemoteAdapter(R.id.lv_course, todayIntent)
            setRemoteAdapter(R.id.lv_course_next_day, tomorrowIntent)
            setEmptyView(R.id.lv_course, R.id.empty)
            setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)
        }
        // 设置小组件的 ID
        todayIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        tomorrowIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // 设置远程适配器
        val (date, weekDay) = getCurrentDateWeekDayWeekNumber() // 获取当前日期和星期以更新小组件显示的时间
        // 更新日期和星期显示
        views.setTextViewText(R.id.tv_date, date)
        views.setTextViewText(R.id.tv_week, weekDay)
        Log.e("更新小组件", "appWidgetId: $appWidgetId")
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

    class WidgetUpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

        override fun doWork(): Result {
            val context = applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, CourseWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            // 调用小组件更新方法
            providerUpdate(context, appWidgetManager, appWidgetIds)

            return Result.success()
        }

        private fun providerUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
                CourseWidgetProvider().onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
