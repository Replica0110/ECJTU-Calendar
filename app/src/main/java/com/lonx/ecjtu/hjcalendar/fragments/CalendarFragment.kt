package com.lonx.ecjtu.hjcalendar.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.recyclerAdapters.*
import com.lonx.ecjtu.hjcalendar.utils.*
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var dateButton: FloatingActionButton
    private var _isRefreshing = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private val refreshInterval: Long = 2000
    private val calendar = Calendar.getInstance() // 创建一个日历实例

    private lateinit var adapter: CourseDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CourseDayAdapter(emptyList(), object : CourseItemAdapter.OnItemClickListener {
            override fun onItemClick(course: CourseInfo, position: Int) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(course.courseName)
                    .setMessage("\n${course.courseTime}\n\n${course.courseWeek}\n\n${course.courseLocation}\n\n${course.courseTeacher}")
                    .show()
            }
        })
        recyclerView.adapter = adapter

        // 初始化ViewModel
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        // 初始化下拉刷新
        swipeRefreshLayout = view.findViewById(R.id.swipe_course_card)
        swipeRefreshLayout.setOnRefreshListener {
            if (!_isRefreshing) {
                _isRefreshing = true
                ToastUtil.showToast(requireContext(), "正在刷新课程信息，请稍后...")
                refreshCourseData()
            } else {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 初始化日期选择按钮
        dateButton = view.findViewById(R.id.fab_date)
        dateButton.setOnClickListener {// 点击选择日期
            showDatePickerDialog()
        }
        dateButton.setOnLongClickListener {
            resetToToday()  // 长按回到今天
            true  // 返回 true 表示事件已处理
        }


        // 观察数据变化
        calendarViewModel.courseList.observe(viewLifecycleOwner) { dayCourseList ->
            adapter.updateData(dayCourseList)
            _isRefreshing = false
            swipeRefreshLayout.isRefreshing = false
        }

        // 检查数据是否存在，不存在则刷新数据
        if (calendarViewModel.courseList.value.isNullOrEmpty()) {
            refreshCourseData()
        }

        return view
    }

    // 日期选择对话框
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                ToastUtil.showToast(requireContext(), "选择日期：$formattedDate")
                refreshCourseData(formattedDate)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    // 更新课程数据
    private fun refreshCourseData(date: String = getCurrentDate()) {
        val sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val weiXinID = sharedPreferences.getString("weixin_id", "")
        swipeRefreshLayout.isRefreshing = true
        // 获取数据
        calendarViewModel.fetchCourseInfo(
            weiXinID ?: "",
            date,
            onSuccess = { message ->
                ToastUtil.showToast(requireContext(), message)
            },
            onFailure = { message ->
                calendar.time = Date() // 失败时不更改日期
                ToastUtil.showToast(requireContext(), "课程获取失败：$message")
            }
        )
        scope.launch {
            delay(refreshInterval)
            _isRefreshing = false
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun resetToToday() {
        calendar.time = Date()  // 重置 Calendar 为今天
        refreshCourseData()     // 刷新课程数据
        ToastUtil.showToast(requireContext(), "已回到今天")
    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
