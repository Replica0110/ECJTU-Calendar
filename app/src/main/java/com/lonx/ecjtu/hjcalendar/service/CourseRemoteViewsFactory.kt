package com.lonx.ecjtu.hjcalendar.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.CourseData

const val EXTRA_COURSE_NAME = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_NAME"
const val EXTRA_COURSE_TIME = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_TIME"
const val EXTRA_COURSE_LOCATION = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_LOCATION"

class CourseRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var courseList: List<CourseData.CourseInfo> = emptyList()

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        val dayCoursesJson = intent.getStringExtra("dayCourses")
        val type = object : TypeToken<CourseData.DayCourses>() {}.type
        val deserializedDayCourses: CourseData.DayCourses? = Gson().fromJson(dayCoursesJson, type)
        courseList = deserializedDayCourses?.courses?.filterNot {
            it.courseName == "课表为空" || it.courseName == "课表加载错误"
        } ?: emptyList()
    }

    override fun onDestroy() {
        courseList = emptyList()
    }

    override fun getCount(): Int {
        return courseList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in courseList.indices) {
            return null
        }

        val course = courseList[position]

        return RemoteViews(context.packageName, R.layout.widget_course_item).apply {
            setTextViewText(R.id.tv_course_name, course.courseName)
            setTextViewText(R.id.tv_course_time, course.courseTime)
            setTextViewText(R.id.tv_course_location, course.courseLocation)

            val fillInIntent = Intent().apply {
                val extras = Bundle()
                extras.putString(EXTRA_COURSE_NAME, course.courseName)
                extras.putString(EXTRA_COURSE_TIME, course.courseTime)
                extras.putString(EXTRA_COURSE_LOCATION, course.courseLocation)
                putExtras(extras)
            }
            setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
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
}