package com.lonx.ecjtu.hjcalendar.service

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.util.CourseData

class CourseListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CourseListRemoteViewsFactory(this.applicationContext, intent)
    }
}


class CourseListRemoteViewsFactory(private val context: Context, private val intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var courseList: ArrayList<CourseData.CourseInfo> = ArrayList()

    override fun onCreate() {
        // TODO 初始化，获取数据
        courseList = ArrayList(getCourseList(context))

    }

    override fun onDataSetChanged() {
        // TODO 数据变更时调用，重新获取数据
        courseList = ArrayList(getCourseList(context))

    }

    override fun onDestroy() {
        // 清理
    }

    override fun getCount(): Int {
        return courseList.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        // 完成课程项的布局和数据绑定
        val course = courseList[position]
        val views = RemoteViews(context.packageName, R.layout.widget_course_item)

        // 设置课程信息
        views.setTextViewText(R.id.tv_course_name, course.courseName)
        views.setTextViewText(R.id.tv_course_time, course.courseTime)
        views.setTextViewText(R.id.tv_course_location, course.courseLocation)

        // 可以为每个 item 添加点击事件

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
    private fun getCourseList(context: Context): List<CourseData.CourseInfo> {
        // 示例数据获取，可以从数据库、API 或本地文件获取
        return listOf(
            CourseData.CourseInfo(
                "电子技术基础(实验)",
                "节次：1,2",
                "上课周：15",
                "地点：数字电路实验室(教20（南区31栋基础实验大楼）911; 教20（南区31栋基础实验大楼）912)",
                "教师：周霞"
            ),
            CourseData.CourseInfo(
                "计算机网络",
                "节次：3,4",
                "上课周：1-16",
                "地点：教20（南区31栋基础实验大楼）911",
                "教师：张三"
            )
        )
    }
}
