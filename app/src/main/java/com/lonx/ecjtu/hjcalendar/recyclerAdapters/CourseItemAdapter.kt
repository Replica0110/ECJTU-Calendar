package com.lonx.ecjtu.hjcalendar.recyclerAdapters

import android.util.Log
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

    // Hitokoto 空课程 ViewHolder
    class HitokotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hitokotoTitle: TextView = itemView.findViewById(R.id.hitokoto_title)
        val hitokotoMessage: TextView = itemView.findViewById(R.id.hitokoto_message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (courseList[position].courseName == "课表为空") VIEW_TYPE_EMPTY else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hitokoto, parent, false)
            HitokotoViewHolder(view)
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
        } else if (holder is HitokotoViewHolder) {
            // 空课程项，显示一言内容
            val randomMessages = listOf(
                "今天没有课程，好好享受空闲时光吧！",
                "课表今天是空的，安排点其他的事情吧。",
                "今天是个放松的好机会，没有课哦！",
                "空课表的日子里，好好休息一下吧。",
                "没有课程安排，何不去运动一下？"
            )
            val randomMessage = randomMessages[Random.nextInt(randomMessages.size)]
            holder.hitokotoTitle.text = "一言"
            holder.hitokotoMessage.text = randomMessage
        }
    }

    override fun getItemCount() = courseList.size

    fun updateData(newCourseList: List<CourseInfo>) {
        courseList = newCourseList
        notifyDataSetChanged()
    }
}

