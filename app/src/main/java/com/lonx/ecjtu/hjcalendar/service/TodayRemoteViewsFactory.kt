package com.lonx.ecjtu.hjcalendar.service

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.CourseData

class TodayRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var todayList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTodayLoading: Boolean = false

    init {
        loadDataInBackground()
    }

    override fun onCreate() {
        Log.e("TodayCourse", "Factory created.")
    }

    override fun onDataSetChanged() {
        loadDataInBackground()
    }

    override fun onDestroy() {
        todayList.clear()
    }

    override fun getCount(): Int {
        return todayList.count { it.courseName != "课表为空" }
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val course = todayList[position]
        return if (course.courseName == "课表为空") {
            null
        } else {
            RemoteViews(context.packageName, R.layout.widget_course_item).apply {
                setTextViewText(R.id.tv_course_name, course.courseName)
                setTextViewText(R.id.tv_course_time, course.courseTime)
                setTextViewText(R.id.tv_course_location, course.courseLocation)
            }
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
        Log.e("TodayCourse", "Loaded courses: $deserializedDayCourses")
        todayList.clear()
        todayList.addAll(deserializedDayCourses.courses)
    }
}
