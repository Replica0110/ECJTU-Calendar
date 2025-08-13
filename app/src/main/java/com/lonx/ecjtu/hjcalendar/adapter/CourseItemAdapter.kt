package com.lonx.ecjtu.hjcalendar.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.databinding.ItemCourseBinding
import com.lonx.ecjtu.hjcalendar.databinding.ItemCourseEmptyBinding
import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.CourseType
import com.lonx.ecjtu.hjcalendar.utils.VectorDrawableUtils

class CourseItemAdapter(
    private val items: List<Course>,
    private var onItemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_NORMAL = 0
        private const val VIEW_TYPE_EMPTY = 1
    }

    interface OnItemClickListener {
        fun onItemClick(course: Course, position: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].type == CourseType.NORMAL) {
            VIEW_TYPE_NORMAL
        } else {
            VIEW_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_EMPTY -> EmptyCourseItemViewHolder(
                ItemCourseEmptyBinding.inflate(inflater, parent, false)
            )
            else -> CourseItemViewHolder(
                ItemCourseBinding.inflate(inflater, parent, false)
            )
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

        @SuppressLint("SetTextI18n")
        fun bind(course: Course) {
            // 设置时间线样式
            setMarker(R.drawable.ic_marker, R.color.primary)
            binding.timeline.lineStyle = TimelineView.LineStyle.NORMAL

            // 绑定课程数据
            binding.apply {
                courseName.text = course.name
                courseTime.text = "节次：${course.time}"
                courseWeek.text = "上课周：${course.week}"
                courseLocation.text = "地点：${course.location}"
                courseTeacher.text = "教师：${course.teacher}"

                // 设置点击事件
                courseItem.setOnClickListener {
                    onItemClickListener?.onItemClick(course, adapterPosition)
                }
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

        fun bind(course: Course) {
            binding.apply {
                when (course.type) {
                    CourseType.NORMAL -> {

                    }
                    CourseType.EMPTY -> {
                        emptyCourseTitle.text = itemView.context.getString(R.string.empty_course_title)
                        emptyCourseMessage.text = course.msg
                    }
                    CourseType.ERROR -> {
                        emptyCourseTitle.text = itemView.context.getString(R.string.error_course_title)
                        emptyCourseMessage.text = course.msg
                    }
                }
            }
        }
    }
}


