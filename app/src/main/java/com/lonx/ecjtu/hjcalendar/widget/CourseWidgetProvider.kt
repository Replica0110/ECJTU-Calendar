package com.lonx.ecjtu.hjcalendar.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.TodayWidgetService
import com.lonx.ecjtu.hjcalendar.service.TomorrowWidgetService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CourseWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // 检测到日期变化或时间调整
        if (intent.action == Intent.ACTION_DATE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            Log.e("日期变化", "intent.action: ${intent.action}")
            // 获取小组件管理器和所有 widgetId
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            // 更新小组件
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            updateHeaderView(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // 从 SharedPreferences 中获取 weiXinID
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val weiXinID = preferences.getString("weixin_id", "")

        // 创建 Intent
        val todayIntent = Intent(context, TodayWidgetService::class.java).apply {
            putExtra("weiXinID", weiXinID)
        }
        val tomorrowIntent = Intent(context, TomorrowWidgetService::class.java).apply {
            putExtra("weiXinID", weiXinID)
        }
        // 创建 RemoteViews，绑定到小组件布局
        val views = RemoteViews(context.packageName, R.layout.widget_course)
        // 设置小组件的 ID
        todayIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        tomorrowIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // 设置远程适配器
        views.setRemoteAdapter(R.id.lv_course, todayIntent)
        views.setRemoteAdapter(R.id.lv_course_next_day, tomorrowIntent)
        views.setEmptyView(R.id.lv_course, R.id.empty)
        views.setEmptyView(R.id.lv_course_next_day, R.id.empty_next_day)

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    private fun updateHeaderView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // 创建 RemoteViews 对象，用于更新父视图
        val headerView = RemoteViews(context.packageName, R.layout.widget_course)

        // 获取当前日期、星期
        val (date, weekDay) = getCurrentDateWeekDayWeekNumber()

        // 设置日期、星期
        headerView.setTextViewText(R.id.tv_date, date)
        headerView.setTextViewText(R.id.tv_week, weekDay)

        // 更新 AppWidget 的父视图
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, headerView)
    }

    private fun getCurrentDateWeekDayWeekNumber(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("M.d", Locale.getDefault())
        val weekDayFormat = SimpleDateFormat("E", Locale.getDefault())

        val date = dateFormat.format(calendar.time)
        val weekDay = weekDayFormat.format(calendar.time)

        return Pair(date, weekDay)
    }

}

