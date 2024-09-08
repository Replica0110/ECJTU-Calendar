package com.lonx.ecjtu.hjcalendar.recyclerAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.CourseInfo
import kotlin.random.Random

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
        return if (courseList[position].courseName == "课表为空") VIEW_TYPE_EMPTY else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty_course, parent, false)
            EmptyCourseViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
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
            val sharedPreferences = holder.itemView.context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
            val emptyCourseText = sharedPreferences.getString("no_course_text", "")
            holder.emptyCourseTitle.text = "轻松一下"
            holder.emptyCourseMessage.text = emptyCourseText
        }
    }

    override fun getItemCount() = courseList.size

}

