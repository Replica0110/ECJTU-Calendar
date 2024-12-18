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
import com.lonx.ecjtu.hjcalendar.adapter.CourseDayAdapter
import com.lonx.ecjtu.hjcalendar.adapter.CourseItemAdapter
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.utils.CourseData.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.*

import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private var isRefreshing = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private val refreshInterval: Long = 2000
    private val calendar = Calendar.getInstance() // 日历实例
    private lateinit var adapter: CourseDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val view = binding.root

        // 初始化RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CourseDayAdapter(emptyList(), object : CourseItemAdapter.OnItemClickListener {
            override fun onItemClick(course: CourseInfo, position: Int) {
                showCourseDetails(course)
            }
        })
        binding.recyclerView.adapter = adapter

        // 日期卡片点击和长按事件
        adapter.setOnDateCardClickListener { position ->
            showDatePickerDialog(position)
        }
        adapter.setOnDateCardLongClickListener {
            resetToToday()
        }

        // 初始化下拉刷新
        binding.swipeCourseCard.setOnRefreshListener {
            handleRefresh()
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
        val bottomSheet = CourseDetailBottomSheetFragment.newInstance(course)
        bottomSheet.show(parentFragmentManager, "CourseDetailBottomSheet")
    }

    // 日期选择对话框，position 用于获取对应日期
    private fun showDatePickerDialog(position: Int) {
        val dayCourseList = calendarViewModel.courseList.value
        if (dayCourseList.isNullOrEmpty() || position !in dayCourseList.indices) return
        val dateStr = dayCourseList[position].date
        val dateRegex = Regex("(\\d{4})-(\\d{2})-(\\d{2})")
        val match = dateRegex.find(dateStr)
        val (year, month, day) = match?.destructured ?: return
        calendar.set(year.toInt(), month.toInt() - 1, day.toInt())
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                calendar.set(y, m, d)
                refreshCourseData(getFormattedDate())
            },
            year.toInt(),
            month.toInt() - 1,
            day.toInt()
        )
        datePickerDialog.show()
    }

    // 更新课程数据
    private fun refreshCourseData(date: String = getCurrentDate()) {
        val weiXinID = getWeixinID()
        binding.swipeCourseCard.isRefreshing = true

        // 获取数据
        calendarViewModel.fetchCourseInfo(
            weiXinID, date,
            onSuccess = { message -> ToastUtil.showToast(requireContext(), message) },
            onFailure = { message -> showErrorToast(message) }
        )

        scope.launch {
            delay(refreshInterval)
            isRefreshing = false
            binding.swipeCourseCard.isRefreshing = false
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
            binding.swipeCourseCard.isRefreshing = false
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
            binding.swipeCourseCard.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}
