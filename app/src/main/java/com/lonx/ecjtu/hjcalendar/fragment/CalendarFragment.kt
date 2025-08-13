package com.lonx.ecjtu.hjcalendar.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lonx.ecjtu.hjcalendar.adapter.CourseDayAdapter
import com.lonx.ecjtu.hjcalendar.adapter.CourseItemAdapter
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.data.model.Course
import com.lonx.ecjtu.hjcalendar.data.model.DailySchedule
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.CalendarViewModel
import com.lonx.ecjtu.hjcalendar.viewmodel.CalendarViewModel.ScheduleUiState
import kotlinx.coroutines.launch
import java.util.*

class CalendarFragment : Fragment() {
    private val viewModel by viewModels<CalendarViewModel>()
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var isShowingCourseDetails = false
    private lateinit var adapter: CourseDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupCollectors()

        // 初始化数据
        viewModel.initialize()
    }

    private fun setupRecyclerView() {
        adapter = CourseDayAdapter(
            scheduleList = emptyList(),
            onItemClickListener = object : CourseItemAdapter.OnItemClickListener {
                override fun onItemClick(course: Course, position: Int) {
                    showCourseDetails(course)
                }
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CalendarFragment.adapter
        }
    }

    private fun setupListeners() {
        // 日期卡片点击和长按事件
        adapter.setOnDateCardClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.currentDate.value.let { currentDate ->
                    showDatePickerDialog(currentDate)
                }
            }
        }
        adapter.setOnDateCardLongClickListener {
            viewModel.resetToToday()
        }

        // 下拉刷新事件
        binding.swipeCourseCard.setOnRefreshListener {
            viewModel.refreshCourses()
        }
    }

    private fun setupCollectors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 收集 UI 状态
                launch {
                    viewModel.uiState.collect { state ->
                        updateUiState(state)
                    }
                }

                // 收集当前日期
                launch {
                    viewModel.currentDate.collect { date ->
                        // 如果需要，可以在这里更新日期显示
                    }
                }
            }
        }

        // 观察一次性事件
        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(requireContext(), message)
            }
        }
    }

    private fun updateUiState(state: ScheduleUiState) {
        binding.swipeCourseCard.isRefreshing = state is ScheduleUiState.Loading

        when (state) {
            is ScheduleUiState.Success -> updateSchedule(state.schedule)
            is ScheduleUiState.Empty -> updateSchedule(state.schedule)
            is ScheduleUiState.Error -> updateSchedule(state.schedule)
            is ScheduleUiState.Loading -> Unit
            is ScheduleUiState.Initial -> Unit
        }
    }

    private fun updateSchedule(schedule: DailySchedule) {
        adapter.updateData(listOf(schedule))
    }

    private fun showCourseDetails(course: Course) {
        if (isShowingCourseDetails) {
            return
        }
        isShowingCourseDetails = true
        val bottomSheet = CourseDetailBottomSheetFragment.newInstance(course)
        bottomSheet.show(parentFragmentManager, "CourseDetailBottomSheet")
        bottomSheet.setOnDismissListener {
            isShowingCourseDetails = false
        }
    }

    private fun showDatePickerDialog(date: Date) {
        val calendar = Calendar.getInstance().apply { time = date }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                viewModel.selectDate(newDate)
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