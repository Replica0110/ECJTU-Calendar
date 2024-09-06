package com.lonx.ecjtu.calendar.recyclers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.utils.CourseInfo

class CourseAdapter(private var courseList: List<CourseInfo>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.courseName)
        val classTime: TextView = itemView.findViewById(R.id.classTime)
        val classWeek: TextView = itemView.findViewById(R.id.classWeek)
        val location: TextView = itemView.findViewById(R.id.location)
        val teacher: TextView = itemView.findViewById(R.id.teacher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courseList[position]
        holder.courseName.text = course.courseName
        holder.classTime.text = course.classTime
        holder.classWeek.text = course.classWeek
        holder.location.text = course.location
        holder.teacher.text = course.teacher
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    // 更新数据的方法
    fun updateData(newCourseList: List<CourseInfo>) {
        courseList = newCourseList
        notifyDataSetChanged()
    }
}
