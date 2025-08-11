package com.lonx.ecjtu.hjcalendar.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    private var courseList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private val VIEW_TYPE_NORMAL = 0
    private val VIEW_TYPE_EMPTY = 1
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
        return courseList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= courseList.size) {
            return null
        }

        val course = courseList[position]
        val viewType = getItemViewType(position)

        return if (viewType == VIEW_TYPE_EMPTY) {
            Log.e("RemoteViewsFactory", "Empty course item created.")
            RemoteViews(context.packageName, R.layout.widget_course_item_empty).apply {
                setTextViewText(R.id.tv_empty_message, course.courseLocation)
            }
        } else { // VIEW_TYPE_NORMAL
            RemoteViews(context.packageName, R.layout.widget_course_item).apply {
                setTextViewText(R.id.tv_course_name, course.courseName)
                setTextViewText(R.id.tv_course_time, course.courseTime)
                setTextViewText(R.id.tv_course_location, course.courseLocation)

                // 设置点击事件的 fillInIntent
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
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }
    fun getItemViewType(position: Int): Int {
        val course = courseList[position]
        return if (course.courseName == "课表为空" || course.courseName == "课表加载错误") {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_NORMAL
        }
    }
    override fun getViewTypeCount(): Int {
        return 2
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
