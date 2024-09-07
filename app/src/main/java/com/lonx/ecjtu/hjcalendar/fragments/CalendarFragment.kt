package com.lonx.ecjtu.hjcalendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.hjcalendar.recyclerAdapters.CalendarAdapter
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel


class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarViewModel: CalendarViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CalendarAdapter(emptyList())
        recyclerView.adapter = adapter

        calendarViewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        calendarViewModel.courseList.observe(viewLifecycleOwner) { courseList ->
            adapter.updateData(courseList)
        }

        // 获取数据之前检查 LiveData 是否已有数据
        if (calendarViewModel.courseList.value.isNullOrEmpty()) {
            // 从 SharedPreferences 获取 URL 和 weiXinID
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val weiXinID = sharedPreferences.getString("weiXinID", "")
            Toast.makeText(requireContext(), "测试", Toast.LENGTH_SHORT).show()
            // 仅在没有数据时调用 fetchCourseInfo
            calendarViewModel.fetchCourseInfo(weiXinID ?: "")
        }

        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

