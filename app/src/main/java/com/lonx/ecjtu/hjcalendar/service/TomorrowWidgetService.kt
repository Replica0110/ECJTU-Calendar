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

class TomorrowWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TomorrowRemoteViewsFactory(this.applicationContext, intent)
    }
}


class TomorrowRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var tomorrowList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTomorrowLoading: Boolean = false

    override fun onCreate() {
        // 初始化，获取数据
        loadDataInBackground()
    }

    override fun onDataSetChanged() {
        // 数据变更时调用，重新获取数据
        if (!isTomorrowLoading) {
            loadDataInBackground()
        }
    }

    override fun onDestroy() {
        // 清理
        tomorrowList.clear()
    }

    override fun getCount(): Int {
        return if (tomorrowList.isEmpty() || tomorrowList.all { it.courseName == "课表为空" }) {
            0
        } else {
            tomorrowList.size
        }
    }


    override fun getViewAt(position: Int): RemoteViews? {
        val course = tomorrowList[position]

        // 检查课程名称是否为 "课表为空"
        if (course.courseName == "课表为空") {
            return null
        }

        // 当前课程有效，创建并返回视图
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
        isTomorrowLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weiXinID = intent.getStringExtra("weiXinID") ?: ""
                val date = getTomorrowDate()
                val html = ECJTUCalendarAPI().getCourseInfo(weiXinID, date) ?: ""
                val dayCourses = ECJTUCalendarAPI().parseHtml(html)
                Log.e("明天课程", "${dayCourses.courses}")
                // 更新数据并通知 UI 更新
                withContext(Dispatchers.Main) {
                    tomorrowList.clear()
                    tomorrowList.addAll(dayCourses.courses)

                    // 仅当数据有变化时才通知 AppWidget 更新
                    if (tomorrowList.isNotEmpty()) {
                        AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(
                            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                            R.id.lv_course_next_day
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTomorrowLoading = false // 标记数据加载结束
            }
        }
    }


    private fun getTomorrowDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        )
        return dateFormat.format(calendar.time)

    }
}
