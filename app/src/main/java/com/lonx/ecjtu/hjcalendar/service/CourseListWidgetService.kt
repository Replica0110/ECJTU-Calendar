package com.lonx.ecjtu.hjcalendar.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.util.CourseData
import com.lonx.ecjtu.hjcalendar.util.ECJTUCalendarAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CourseListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CourseListRemoteViewsFactory(this.applicationContext, intent)
    }
}


class CourseListRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var courseList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isDataLoading: Boolean = false

    override fun onCreate() {
        // 初始化，获取数据
        loadDataInBackground()
    }

    override fun onDataSetChanged() {
        // 数据变更时调用，重新获取数据
        if (!isDataLoading) {
            loadDataInBackground()
        }
    }

    override fun onDestroy() {
        // 清理
        courseList.clear()
    }

    override fun getCount(): Int {
        return courseList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val course = courseList[position]
        val views = RemoteViews(context.packageName, R.layout.widget_course_item)
        views.setTextViewText(R.id.tv_course_name, course.courseName)
        views.setTextViewText(R.id.tv_course_time, course.courseTime)
        views.setTextViewText(R.id.tv_course_location, course.courseLocation)

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private fun loadDataInBackground() {
        isDataLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weiXinID = intent.getStringExtra("weiXinID") ?: ""
                val date = intent.getStringExtra("date") ?: ""
                val html = ECJTUCalendarAPI().getCourseInfo(weiXinID, date) ?: ""
                val dayCourses = ECJTUCalendarAPI().parseHtml(html)

                // 更新数据并通知 UI 更新
                withContext(Dispatchers.Main) {
                    courseList.clear()
                    courseList.addAll(dayCourses.courses)

                    // 仅当数据有变化时才通知 AppWidget 更新
                    if (courseList.isNotEmpty()) {
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(
                            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                            R.id.lv_today_courses
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isDataLoading = false // 标记数据加载结束
            }
        }
    }
}
