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

class CourseRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var courseList: ArrayList<CourseData.CourseInfo> = ArrayList()

    init {
        loadDataInBackground()
    }

    override fun onCreate() {
        Log.e("RemoteViewsFactory", "Factory created.")
    }

    override fun onDataSetChanged() {
        loadDataInBackground()
    }

    override fun onDestroy() {
        courseList.clear()
    }

    override fun getCount(): Int {
        return courseList.count { it.courseName != "课表为空" && it.courseName != "课表加载错误"}
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val course = courseList[position]
        return if (course.courseName == "课表为空" || course.courseName == "课表加载错误") {
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
        val dayCourses = intent.getStringExtra("dayCourses")
        val type = object : TypeToken<CourseData.DayCourses>() {}.type
        val deserializedDayCourses: CourseData.DayCourses = Gson().fromJson(dayCourses, type)
//        Log.e("TodayCourse", "Loaded courses: $deserializedDayCourses")
        courseList.clear()
        courseList.addAll(deserializedDayCourses.courses)
    }
}
