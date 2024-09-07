package com.lonx.ecjtu.hjcalendar.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.recyclerAdapters.CalendarAdapter
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var _isRefreshing = false

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
                _isRefreshing = true
                Toast.makeText(requireContext(), "正在刷新课程信息，请稍后...", Toast.LENGTH_SHORT).show()
                refreshCourseData()
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
        // 读取之前的输入值并设置到输入框
        val weiXinID = sharedPreferences.getString("weixin_id", "")  // 使用固定键
        Log.e("CalendarFragment", "WeiXinID: $weiXinID")
        calendarViewModel.fetchCourseInfo(weiXinID ?: "", object : () -> Unit {
            override fun invoke() {
                _isRefreshing = false // 数据加载完成后取消标记
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
