package com.lonx.ecjtu.hjcalendar.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.preference.PreferenceManager
import androidx.room.util.copy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

class TomorrowRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var tomorrowList: ArrayList<CourseData.CourseInfo> = ArrayList()
    private var isTomorrowLoading: Boolean = false

    override fun onCreate() {
        // 初始化，获取数据
        loadDataInBackground()
        Log.e("明天课程", "onCreate")

    }

    override fun onDataSetChanged() {
        // 数据变更时调用，重新获取数据
        loadDataInBackground()
        Log.e("明天课程", "onDataSetChanged")

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
        isTomorrowLoading = true

                val dayCourses = intent.getStringExtra("dayCourses")
                val type = object : TypeToken<CourseData.DayCourses>() {}.type
                val deserializedDayCourses: CourseData.DayCourses = Gson().fromJson(dayCourses, type)
                Log.e("明天课程", "Get courses：$deserializedDayCourses")


                    tomorrowList.clear()
                    for (course in deserializedDayCourses.courses) {
                        tomorrowList.add(course)
                    }

                }

        }



