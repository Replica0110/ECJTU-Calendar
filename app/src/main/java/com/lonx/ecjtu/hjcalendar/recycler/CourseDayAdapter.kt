package com.lonx.ecjtu.hjcalendar.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.util.CourseData.DayCourses


class CourseDayAdapter(
    private var dayCourseList: List<DayCourses>,
    private var onItemClickListener: CourseItemAdapter.OnItemClickListener?
) : RecyclerView.Adapter<CourseDayAdapter.DayCourseViewHolder>() {

    class DayCourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.day_text_view)
        val courseRecyclerView: RecyclerView = itemView.findViewById(R.id.course_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayCourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_course, parent, false)
        return DayCourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayCourseViewHolder, position: Int) {
        val dayCourseInfo = dayCourseList[position]
        holder.dayTextView.text = dayCourseInfo.date

        // 设置RecyclerView嵌套用于显示当天的课程
        val courseAdapter = CourseItemAdapter(dayCourseInfo.courses, onItemClickListener)
        holder.courseRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.courseRecyclerView.adapter = courseAdapter
    }

    override fun getItemCount() = dayCourseList.size

    fun updateData(newDayCourseList: List<DayCourses>) {
        dayCourseList = newDayCourseList
        notifyDataSetChanged()
    }

}
