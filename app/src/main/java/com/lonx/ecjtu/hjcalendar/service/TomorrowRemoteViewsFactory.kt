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

class TomorrowRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var tomorrowList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTomorrowLoading: Boolean = false

    init {
        loadDataInBackground()
    }

    override fun onCreate() {
        Log.e("TomorrowCourse", "Factory created.")
    }

    override fun onDataSetChanged() {
        Log.e("TomorrowCourse", "onDataSetChanged")
        loadDataInBackground()
    }

    override fun onDestroy() {
        tomorrowList.clear()
    }

    override fun getCount(): Int {
        return tomorrowList.count { it.courseName != "课表为空" }
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val course = tomorrowList[position]
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

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun loadDataInBackground() {
        isTomorrowLoading = true
        val dayCourses = intent.getStringExtra("dayCourses")
        val type = object : TypeToken<CourseData.DayCourses>() {}.type
        val deserializedDayCourses: CourseData.DayCourses = Gson().fromJson(dayCourses, type)
        Log.e("TomorrowCourse", "Loaded courses: $deserializedDayCourses")
        tomorrowList.clear()
        tomorrowList.addAll(deserializedDayCourses.courses)
    }
}
