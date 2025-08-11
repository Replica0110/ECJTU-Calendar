package com.lonx.ecjtu.hjcalendar.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.databinding.ItemDayCoursesBinding
import com.lonx.ecjtu.hjcalendar.utils.CourseData.DayCourses

class CourseDayAdapter(
    private var dayCourseList: List<DayCourses>,
    private var onItemClickListener: CourseItemAdapter.OnItemClickListener?,
    private var onDateCardClickListener: ((position: Int) -> Unit)? = null,
    private var onDateCardLongClickListener: ((position: Int) -> Unit)? = null
) : RecyclerView.Adapter<CourseDayAdapter.DayCourseViewHolder>() {

    class DayCourseViewHolder(val binding: ItemDayCoursesBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayCourseViewHolder {
        val binding = ItemDayCoursesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayCourseViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DayCourseViewHolder, position: Int) {
        val dayCourseInfo = dayCourseList[position]
        val dateRegex = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+([^（]+)（第(\\d+)周）")
        val match = dateRegex.find(dayCourseInfo.date)
        if (match != null) {
            val (date, weekDay, weekNum) = match.destructured
            Log.e("CourseDayAdapter", "Date: $date, Week Day: $weekDay, Week Num: $weekNum")
            holder.binding.dayTextView.text = date
            holder.binding.weekInfoTextView.text = "$weekDay · 第${weekNum}周"
        } else {
            holder.binding.dayTextView.text = dayCourseInfo.date
            holder.binding.weekInfoTextView.text = ""
        }
        // 日期卡片点击和长按
        holder.binding.dateCard.setOnClickListener {
            onDateCardClickListener?.invoke(position)
        }
        holder.binding.dateCard.setOnLongClickListener {
            onDateCardLongClickListener?.invoke(position)
            true
        }
        // 设置RecyclerView嵌套用于显示当天的课程
        val courseAdapter = CourseItemAdapter(dayCourseInfo.courses, onItemClickListener)
        holder.binding.courseRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = courseAdapter
            setHasFixedSize(true)
        }
    }

    override fun getItemCount() = dayCourseList.size

    fun updateData(newDayCourseList: List<DayCourses>) {
        dayCourseList = newDayCourseList
        notifyDataSetChanged()
    }

    fun setOnDateCardClickListener(listener: ((position: Int) -> Unit)?) {
        onDateCardClickListener = listener
    }
    fun setOnDateCardLongClickListener(listener: ((position: Int) -> Unit)?) {
        onDateCardLongClickListener = listener
    }
}
