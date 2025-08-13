package com.lonx.ecjtu.hjcalendar.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.databinding.ItemDayCoursesBinding
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule

class CourseDayAdapter(
    private var scheduleList: List<DailySchedule>,
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
        val schedule = scheduleList[position]
        val dateRegex = Regex("(\\d{4}-\\d{2}-\\d{2})\\s+([^（]+)（第(\\d+)周）")
        val match = dateRegex.find(schedule.dateInfo)
        
        holder.binding.apply {
            if (match != null) {
                val (date, weekDay, weekNum) = match.destructured
                Log.d("CourseDayAdapter", "Date: $date, Week Day: $weekDay, Week Num: $weekNum")
                dayTextView.text = date
                weekInfoTextView.text = "$weekDay · 第${weekNum}周"
            } else {
                dayTextView.text = schedule.dateInfo
                weekInfoTextView.text = ""
            }

            // 日期卡片点击和长按
            dateCard.setOnClickListener {
                onDateCardClickListener?.invoke(position)
            }
            dateCard.setOnLongClickListener {
                onDateCardLongClickListener?.invoke(position)
                true
            }

            // 设置RecyclerView嵌套用于显示当天的课程
            courseRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = CourseItemAdapter(schedule.courses, onItemClickListener)
                setHasFixedSize(true)
            }
        }
    }

    override fun getItemCount() = scheduleList.size

    fun updateData(newScheduleList: List<DailySchedule>) {
        scheduleList = newScheduleList
        notifyDataSetChanged()
    }

    fun setOnDateCardClickListener(listener: ((position: Int) -> Unit)?) {
        onDateCardClickListener = listener
    }

    fun setOnDateCardLongClickListener(listener: ((position: Int) -> Unit)?) {
        onDateCardLongClickListener = listener
    }
}
