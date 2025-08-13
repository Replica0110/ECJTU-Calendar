package com.lonx.ecjtu.hjcalendar.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCourseDetailBottomSheetBinding
import com.lonx.ecjtu.hjcalendar.data.model.Course

class CourseDetailBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCourseDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_COURSE = "arg_course"
        fun newInstance(course: Course): CourseDetailBottomSheetFragment {
            val fragment = CourseDetailBottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailBottomSheetBinding.inflate(inflater, container, false)
        val course = if (SDK_INT >= TIRAMISU) {
            arguments?.getParcelable(ARG_COURSE, Course::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_COURSE)
        }
        
        binding.apply {
            tvCourseName.text = course?.name
            tvCourseTime.text = "节次：${course?.time}"
            tvCourseWeek.text = "上课周：${course?.week}"
            tvCourseLocation.text = "地点：${course?.location}"
            tvCourseTeacher.text = "教师：${course?.teacher}"
        }
        
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 