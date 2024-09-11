package com.lonx.ecjtu.hjcalendar.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.CourseListWidgetService
import com.lonx.ecjtu.hjcalendar.util.CourseData
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
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val weiXinID = preferences.getString("weixin_id", "") // 替换为实际值
        val date = getCurrentDate() // 替换为实际值

        // 创建 Intent
        val intent = Intent(context, CourseListWidgetService::class.java).apply {
            putExtra("weiXinID", weiXinID)
            putExtra("date", date)
        }
        // 创建 RemoteViews，绑定到小组件布局
        val views = RemoteViews(context.packageName, R.layout.widget_course)

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        views.setRemoteAdapter(R.id.lv_today_courses, intent)

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}

