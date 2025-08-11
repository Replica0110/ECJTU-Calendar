package com.lonx.ecjtu.hjcalendar.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lonx.ecjtu.hjcalendar.adapter.CourseDayAdapter
import com.lonx.ecjtu.hjcalendar.adapter.CourseItemAdapter
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.utils.CourseData.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.CalendarViewModel
import java.util.*

class CalendarFragment : Fragment() {
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CourseDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        // 初始化数据
        calendarViewModel.initialize()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CourseDayAdapter(emptyList(), object : CourseItemAdapter.OnItemClickListener {
            override fun onItemClick(course: CourseInfo, position: Int) {
                showCourseDetails(course)
            }
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        // 日期卡片点击和长按事件
        adapter.setOnDateCardClickListener {
            // 从 ViewModel 获取当前日期来初始化 DatePicker
            calendarViewModel.currentDate.value?.let { currentDate ->
                showDatePickerDialog(currentDate)
            }
        }
        adapter.setOnDateCardLongClickListener {
            calendarViewModel.resetToToday()
        }

        // 下拉刷新事件
        binding.swipeCourseCard.setOnRefreshListener {
            calendarViewModel.refreshCourses()
        }
    }

    private fun setupObservers() {
        // 观察课程列表数据变化
        calendarViewModel.courseList.observe(viewLifecycleOwner) { dayCourseList ->
            adapter.updateData(dayCourseList)
        }

        // 观察加载状态变化，控制刷新动画
        calendarViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeCourseCard.isRefreshing = isLoading
        }

        // 观察 Toast 消息事件
        calendarViewModel.toastMessage.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(requireContext(), message)
            }
        }
    }

    private fun showCourseDetails(course: CourseInfo) {
        val bottomSheet = CourseDetailBottomSheetFragment.newInstance(course)
        bottomSheet.show(parentFragmentManager, "CourseDetailBottomSheet")
    }

    private fun showDatePickerDialog(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(year, month, dayOfMonth)
                // 通知 ViewModel 日期已改变
                calendarViewModel.selectDate(newCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}