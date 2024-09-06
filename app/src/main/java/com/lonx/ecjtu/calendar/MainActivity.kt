package com.lonx.ecjtu.calendar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.calendar.databinding.ActivityMainBinding
import com.lonx.ecjtu.calendar.viewmodels.CourseViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // 调用父类的 onCreate 方法

        // 使用布局充气器初始化绑定对象
        binding = ActivityMainBinding.inflate(layoutInflater)
        // 设置内容视图
        setContentView(binding.root)

        // 获取底部导航栏视图
        val navView: BottomNavigationView = binding.navView

        // 获取 NavController 对象，用于管理应用内的导航
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 创建 AppBarConfiguration 对象，指定顶部栏的导航项
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_calendar,
                R.id.navigation_settings
            )
        )

        // 将顶部栏与 NavController 关联起来，实现顶部栏的导航功能
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 将底部导航栏与 NavController 关联起来，实现底部导航的功能
        navView.setupWithNavController(navController)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            val weiXinID = sharedPreferences.getString("weiXinID", "")
            // 仅在首次创建 Activity 时自动更新课程信息
            if (savedInstanceState == null && !weiXinID.isNullOrEmpty()) {
                val courseViewModel = ViewModelProvider(this)[CourseViewModel::class.java]
                courseViewModel.updateCourseInfo(weiXinID)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing CourseViewModel: ${e.message}")
        }
    }
}
