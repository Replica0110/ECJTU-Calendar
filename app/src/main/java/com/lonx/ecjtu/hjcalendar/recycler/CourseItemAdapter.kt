package com.lonx.ecjtu.hjcalendar.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.util.CourseData.CourseInfo

class CourseItemAdapter(
    private var courseList: List<CourseInfo>,
    private var onItemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_NORMAL = 0
    private val VIEW_TYPE_EMPTY = 1

    interface OnItemClickListener {
        fun onItemClick(course: CourseInfo, position: Int)
    }

    // 普通课程 ViewHolder
    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.course_name)
        val courseTime: TextView = itemView.findViewById(R.id.course_time)
        val courseWeek: TextView = itemView.findViewById(R.id.course_week)
        val courseLocation: TextView = itemView.findViewById(R.id.course_location)
        val courseTeacher: TextView = itemView.findViewById(R.id.course_teacher)
    }

    // 空课程 ViewHolder
    class EmptyCourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyCourseTitle: TextView = itemView.findViewById(R.id.empty_course_title)
        val emptyCourseMessage: TextView = itemView.findViewById(R.id.empty_course_message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (courseList[position].courseName == "课表为空" || courseList[position].courseName == "课表加载错误") VIEW_TYPE_EMPTY else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_empty_course, parent, false)
            EmptyCourseViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_course, parent, false)
            CourseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val course = courseList[position]

        if (holder is CourseViewHolder) {
            // 普通课程项
            holder.courseName.text = course.courseName
            holder.courseTime.text = course.courseTime
            holder.courseWeek.text = course.courseWeek
            holder.courseLocation.text = course.courseLocation
            holder.courseTeacher.text = course.courseTeacher

            holder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(course, position)
            }
        } else if (holder is EmptyCourseViewHolder) {
            // 使用 PreferenceManager 统一访问 SharedPreferences
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)
            if (course.courseName == "课表为空") {
                // 获取空课表的自定义文本
                val emptyCourseText = sharedPreferences.getString(
                    "no_course_text",
                    holder.itemView.context.getString(R.string.empty_course_message)
                )
                holder.emptyCourseTitle.text =
                    holder.itemView.context.getString(R.string.empty_course_title)
                holder.emptyCourseMessage.text = emptyCourseText
            } else if (course.courseName == "课表加载错误") {
                holder.emptyCourseTitle.text =
                    holder.itemView.context.getString(R.string.error_course_title)
                holder.emptyCourseMessage.text =
                    holder.itemView.context.getString(R.string.error_course_message)
            }
        }
    }

    override fun getItemCount() = courseList.size
}
