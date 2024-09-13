package com.lonx.ecjtu.hjcalendar.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.util.CourseData

class TodayRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var todayList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTodayLoading: Boolean = false

    override fun onCreate() {

        loadDataInBackground()
    }

    override fun onDataSetChanged() {
        loadDataInBackground()
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
        return RemoteViews(context.packageName, R.layout.widget_course_item).apply {
            setTextViewText(R.id.tv_course_name, course.courseName)
            setTextViewText(R.id.tv_course_time, course.courseTime)
            setTextViewText(R.id.tv_course_location, course.courseLocation)
        }
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
                val dayCourses = intent.getStringExtra("dayCourses")
                val type = object : TypeToken<CourseData.DayCourses>() {}.type
                val deserializedDayCourses: CourseData.DayCourses = Gson().fromJson(dayCourses, type)
                Log.e("今天课程", "Get courses: $deserializedDayCourses")
                // 更新数据并通知 UI 更新
                    todayList.clear()
                    for (course in deserializedDayCourses.courses){
                        todayList.add(course)
                    }

                }
}


