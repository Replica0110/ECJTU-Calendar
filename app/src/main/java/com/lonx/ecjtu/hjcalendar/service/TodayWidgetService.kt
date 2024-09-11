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
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodayWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodayRemoteViewsFactory(this.applicationContext, intent)
    }
}


class TodayRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var todayList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTodayLoading: Boolean = false

    override fun onCreate() {
        // 初始化，获取数据
        loadDataInBackground()
    }

    override fun onDataSetChanged() {
        // 数据变更时调用，重新获取数据
        if (!isTodayLoading) {
            loadDataInBackground()
        }
    }

    override fun onDestroy() {
        // 清理
        todayList.clear()
    }

    override fun getCount(): Int {
        return if (todayList.isEmpty() || todayList.all { it.courseName == "课表为空" }) {
            0
        } else {
            todayList.size
        }
    }


    override fun getViewAt(position: Int): RemoteViews? {
        val course = todayList[position]

        // 检查数据是否为空，如果为空，则返回 null
        if (course.courseName == "课表为空") {
            return null
        }

        // 当前位置的数据有效，则设置 RemoteViews
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
        isTodayLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weiXinID = intent.getStringExtra("weiXinID") ?: ""
                val date =getTodayDate()
                val html = ECJTUCalendarAPI().getCourseInfo(weiXinID, date) ?: ""

                val dayCourses = ECJTUCalendarAPI().parseHtml(html)
                Log.e("今天课程", "${dayCourses.courses}")
                Log.e("日期", "今天是：${dayCourses.date}")

                // 更新数据并通知 UI 更新
                withContext(Dispatchers.Main) {
                    todayList.clear()
                    todayList.addAll(dayCourses.courses)

                    // 仅当数据有变化时才通知 AppWidget 更新
                    if (todayList.isNotEmpty()) {
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(
                            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                            R.id.lv_course
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTodayLoading = false // 标记数据加载结束
            }
        }
    }

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)

    }
    // TODO 处理获取的dayCourses.date，格式为“2024-09-11 星期三（第2周）”，获取日期，示例格式为“3.26”，获取星期，示例格式为“周四”，获取周数，示例格式为“第2周”
}
