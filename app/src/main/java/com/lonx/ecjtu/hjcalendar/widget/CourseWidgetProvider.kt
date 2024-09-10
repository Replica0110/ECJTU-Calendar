package com.lonx.ecjtu.hjcalendar.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.service.CourseListWidgetService
import com.lonx.ecjtu.hjcalendar.util.CourseData

class CourseWidgetProvider : AppWidgetProvider() {

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
        // 创建 RemoteViews，绑定到小组件布局
        val views = RemoteViews(context.packageName, R.layout.widget_course_list)

        // 绑定适配器 RemoteViewsService
        val intent = Intent(context, CourseListWidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        views.setRemoteAdapter(R.id.widget_course_list_view, intent)

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

