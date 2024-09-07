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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.recyclerAdapters.CalendarAdapter
import com.lonx.ecjtu.hjcalendar.utils.CourseInfo
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var _isRefreshing = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private val refreshInterval: Long = 3000

    private lateinit var adapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CalendarAdapter(emptyList(), object : CalendarAdapter.OnItemClickListener {
            override fun onItemClick(course: CourseInfo, position: Int) {
                Log.e("CalendarFragment", "点击了课程: ${course.courseName}，位置: $position")
                // 在这里处理点击事件，打开一个
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(course.courseName)
                    .setMessage("\n${course.classTime}\n\n${course.classWeek}\n\n${course.location}\n\n${course.teacher}")
                    .show()
            }
        })
        recyclerView.adapter = adapter

        // 初始化 ViewModel
        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        // 初始化 SwipeRefreshLayout
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

        // 观察 courseList 数据变化
        calendarViewModel.courseList.observe(viewLifecycleOwner) { courseList ->
            adapter.updateData(courseList)
        }

        // 获取数据之前检查 LiveData 是否已有数据
        if (calendarViewModel.courseList.value.isNullOrEmpty()) {
            ToastUtil.showToast(requireContext(), "正在刷新课程信息，请稍后...")
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
            override fun invoke() {}
        })
        scope.launch {
            delay(refreshInterval)
            _isRefreshing = false
            swipeRefreshLayout.isRefreshing = false // 刷新结束，取消动画
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }
}
