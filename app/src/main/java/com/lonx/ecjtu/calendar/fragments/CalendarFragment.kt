package com.lonx.ecjtu.calendar.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lonx.ecjtu.calendar.recyclers.CourseAdapter
import com.lonx.ecjtu.calendar.databinding.FragmentCalendarBinding
import com.lonx.ecjtu.calendar.viewmodels.CourseViewModel


class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CourseAdapter
    private lateinit var courseViewModel: CourseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        courseViewModel = ViewModelProvider(requireActivity())[CourseViewModel::class.java]

        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = CourseAdapter(emptyList())
        recyclerView.adapter = adapter

        val currentCourseList = courseViewModel.courseList
        adapter.updateData(currentCourseList)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
