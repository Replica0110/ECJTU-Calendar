package com.lonx.ecjtu.hjcalendar.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lonx.ecjtu.hjcalendar.databinding.FragmentCourseDetailBottomSheetBinding
import com.lonx.ecjtu.hjcalendar.utils.CourseData

class CourseDetailBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCourseDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_COURSE = "arg_course"
        fun newInstance(course: CourseData.CourseInfo): CourseDetailBottomSheetFragment {
            val fragment = CourseDetailBottomSheetFragment()
            val args = Bundle()
            args.putParcelable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCourseDetailBottomSheetBinding.inflate(inflater, container, false)
        val course = arguments?.getParcelable<CourseData.CourseInfo>(ARG_COURSE)
        binding.tvCourseName.text = course?.courseName
        binding.tvCourseTime.text = course?.courseTime
        binding.tvCourseWeek.text = course?.courseWeek
        binding.tvCourseLocation.text = course?.courseLocation
        binding.tvCourseTeacher.text = course?.courseTeacher
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 