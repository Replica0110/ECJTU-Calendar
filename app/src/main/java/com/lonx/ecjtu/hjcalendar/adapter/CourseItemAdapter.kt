package com.lonx.ecjtu.hjcalendar.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.databinding.ItemCourseBinding
import com.lonx.ecjtu.hjcalendar.databinding.ItemCourseEmptyBinding
import com.lonx.ecjtu.hjcalendar.utils.CourseData.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.VectorDrawableUtils

class CourseItemAdapter(
    private val items: List<CourseInfo>,
    private var onItemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_NORMAL = 0
    private val VIEW_TYPE_EMPTY = 1

    interface OnItemClickListener {
        fun onItemClick(course: CourseInfo, position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].courseName == "课表为空" || items[position].courseName == "课表加载错误") {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val binding = ItemCourseEmptyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            EmptyCourseItemViewHolder(binding)
        } else {
            val binding = ItemCourseBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            CourseItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val course = items[position]
        when (holder) {
            is CourseItemViewHolder -> {
                holder.binding.timeline.initLine(
                    TimelineView.getTimeLineViewType(position, itemCount)
                )
                holder.bind(course)
            }
            is EmptyCourseItemViewHolder -> holder.bind(course)
        }
    }

    override fun getItemCount() = items.size

    inner class CourseItemViewHolder(
        val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: CourseInfo) {
            // 设置时间线样式
            setMarker(R.drawable.ic_marker, R.color.primary)
            binding.timeline.lineStyle = TimelineView.LineStyle.NORMAL

            // 绑定课程数据
            binding.courseName.text = course.courseName
            binding.courseTime.text = course.courseTime
            binding.courseWeek.text = course.courseWeek
            binding.courseLocation.text = course.courseLocation
            binding.courseTeacher.text = course.courseTeacher

            // 设置点击事件
            binding.courseItem.setOnClickListener {
                onItemClickListener?.onItemClick(course, adapterPosition)
            }
        }

        private fun setMarker(drawableResId: Int, colorFilter: Int) {
            binding.timeline.marker = VectorDrawableUtils.getDrawable(
                binding.timeline.context,
                drawableResId,
                ContextCompat.getColor(binding.timeline.context, colorFilter)
            )
        }
    }

    inner class EmptyCourseItemViewHolder(
        private val binding: ItemCourseEmptyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: CourseInfo) {
            if (course.courseName.contains("课表为空")) {
                binding.emptyCourseTitle.text = itemView.context.getString(R.string.empty_course_title)
                binding.emptyCourseMessage.text = course.courseLocation
            } else if (course.courseName.contains("课表加载错误")) {
                binding.emptyCourseTitle.text = itemView.context.getString(R.string.error_course_title)
                binding.emptyCourseMessage.text = course.courseLocation
            }
        }
    }
}


