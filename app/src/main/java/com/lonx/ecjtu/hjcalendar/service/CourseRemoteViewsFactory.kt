package com.lonx.ecjtu.hjcalendar.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule

const val EXTRA_COURSE_NAME = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_NAME"
const val EXTRA_COURSE_TIME = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_TIME"
const val EXTRA_COURSE_LOCATION = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_LOCATION"
const val EXTRA_COURSE_TEACHER = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_TEACHER"
const val EXTRA_COURSE_WEEK = "com.lonx.ecjtu.hjcalendar.widget.EXTRA_COURSE_WEEK"

class CourseRemoteViewsFactory(
    private val context: Context, 
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var courseList: List<Course> = emptyList()

    override fun onCreate() {
        // 初始化时不需要加载数据
    }

    override fun onDataSetChanged() {
        val scheduleJson = intent.getStringExtra("schedule")
        val type = object : TypeToken<DailySchedule>() {}.type
        val schedule: DailySchedule? = Gson().fromJson(scheduleJson, type)
        courseList = schedule?.courses?.filterNot {
            it.name == "课表为空" || it.name == "课表加载错误"
        } ?: emptyList()
    }

    override fun onDestroy() {
        courseList = emptyList()
    }

    override fun getCount(): Int = courseList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in courseList.indices) return null

        val course = courseList[position]

        return RemoteViews(context.packageName, R.layout.widget_course_item).apply {
            setTextViewText(R.id.tv_course_name, course.name)
            setTextViewText(R.id.tv_course_time, "节次：${course.time}")
            setTextViewText(R.id.tv_course_location, "地点：${course.location}")

            // 创建点击意图
            val fillInIntent = Intent().apply {
                putExtras(Bundle().apply {
                    putString(EXTRA_COURSE_NAME, course.name)
                    putString(EXTRA_COURSE_TIME, course.time)
                    putString(EXTRA_COURSE_LOCATION, course.location)
                    putString(EXTRA_COURSE_TEACHER, course.teacher)
                    putString(EXTRA_COURSE_WEEK, course.week)
                })
            }
            setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}