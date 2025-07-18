package com.lonx.ecjtu.hjcalendar.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.adapter.CourseDayAdapter
import com.lonx.ecjtu.hjcalendar.adapter.CourseItemAdapter
import com.lonx.ecjtu.hjcalendar.utils.CourseData.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewModel.CalendarViewModel
import kotlinx.coroutines.*

import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var dateButton: FloatingActionButton
    private var isRefreshing = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private val refreshInterval: Long = 2000
    private val calendar = Calendar.getInstance() // 日历实例
    private lateinit var adapter: CourseDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // 初始化RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CourseDayAdapter(emptyList(), object : CourseItemAdapter.OnItemClickListener {
            override fun onItemClick(course: CourseInfo, position: Int) {
                showCourseDetails(course)
            }
        })
        recyclerView.adapter = adapter


        // 初始化下拉刷新
        swipeRefreshLayout = view.findViewById(R.id.swipe_course_card)
        swipeRefreshLayout.setOnRefreshListener {
            handleRefresh()
        }

        // 初始化日期选择按钮
        dateButton = view.findViewById(R.id.fab_date)
        dateButton.setOnClickListener {
            showDatePickerDialog()
        }
        dateButton.setOnLongClickListener {
            resetToToday()
            true
        }

        // 观察数据变化
        observeCourseList()

        // 如果数据为空，则刷新
        if (calendarViewModel.courseList.value.isNullOrEmpty()) {
            refreshCourseData()
        }

        return view
    }

    // 显示课程详情对话框
    private fun showCourseDetails(course: CourseInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(course.courseName)
            .setMessage("\n${course.courseTime}\n\n${course.courseWeek}\n\n${course.courseLocation}\n\n${course.courseTeacher}")
            .show()
    }


    // 日期选择对话框
    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                refreshCourseData(getFormattedDate())
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // 更新课程数据
    private fun refreshCourseData(date: String = getCurrentDate()) {
        val weiXinID = getWeixinID()
        swipeRefreshLayout.isRefreshing = true

        // 获取数据
        calendarViewModel.fetchCourseInfo(
            weiXinID, date,
            onSuccess = { message -> ToastUtil.showToast(requireContext(), message) },
            onFailure = { message -> showErrorToast(message) }
        )

        scope.launch {
            delay(refreshInterval)
            isRefreshing = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getWeixinID(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return sharedPreferences.getString("weixin_id", "") ?: ""
    }

    // 刷新处理逻辑
    private fun handleRefresh() {
        if (!isRefreshing) {
            isRefreshing = true
            ToastUtil.showToast(requireContext(), "正在刷新课程信息，请稍后...")
            refreshCourseData()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    // 获取当前日期的格式化字符串
    private fun getCurrentDate(): String {
        return getFormattedDate()
    }

    private fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }


    // 显示错误提示
    private fun showErrorToast(message: String) {
        calendar.time = Date() // 失败时不更改日期
        ToastUtil.showToast(requireContext(), "课程获取失败：$message")
    }

    // 重置到今天并刷新
    private fun resetToToday() {
        calendar.time = Date()
        refreshCourseData()
        ToastUtil.showToast(requireContext(), "已显示今天的课程")
    }

    // 观察课程列表数据
    private fun observeCourseList() {
        calendarViewModel.courseList.observe(viewLifecycleOwner) { dayCourseList ->
            adapter.updateData(dayCourseList)
            isRefreshing = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
