package com.lonx.ecjtu.hjcalendar.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.recyclerAdapters.CalendarAdapter
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var _isRefreshing = false
    private var lastRefreshTime: Long = 0 // 记录上一次刷新的时间
    private val refreshInterval: Long = 5000 // 设置刷新间隔为 5 秒

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CalendarAdapter(emptyList())
        recyclerView.adapter = adapter

        // 初始化 ViewModel
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        // 初始化 SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_course_card)
        swipeRefreshLayout.setOnRefreshListener {
            if (!_isRefreshing) {
                val currentTime = System.currentTimeMillis()

                // 检查是否超过了规定的刷新间隔
                if (currentTime - lastRefreshTime >= refreshInterval) {
                    _isRefreshing = true
                    lastRefreshTime = currentTime
                    ToastUtil.showToast(requireContext(), "正在刷新课程信息，请稍后...")
                    refreshCourseData()
                } else {
                    // 提示用户等待刷新间隔
                    val remainingTime = (refreshInterval - (currentTime - lastRefreshTime)) / 1000
                    ToastUtil.showToast(requireContext(), "请等待 $remainingTime 秒后再刷新")

                    // 手动停止刷新动画
                    swipeRefreshLayout.isRefreshing = false
                }
            } else {
                // 如果正在刷新，也停止动画
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 观察 courseList 数据变化
        calendarViewModel.courseList.observe(viewLifecycleOwner) { courseList ->
            adapter.updateData(courseList)
        }

        // 获取数据之前检查 LiveData 是否已有数据
        if (calendarViewModel.courseList.value.isNullOrEmpty()) {
            refreshCourseData()  // 第一次加载时获取数据
        }

        return view
    }

    private fun refreshCourseData() {
        // 初始化 SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val weiXinID = sharedPreferences.getString("weixin_id", "")  // 使用固定键
        Log.e("CalendarFragment", "WeiXinID: $weiXinID")

        // 调用 ViewModel 来获取课程信息
        calendarViewModel.fetchCourseInfo(weiXinID ?: "", object : () -> Unit {
            override fun invoke() {
                _isRefreshing = false // 刷新完成后，取消标记
                swipeRefreshLayout.isRefreshing = false // 刷新结束，取消动画
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

