package com.lonx.ecjtu.hjcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.hjcalendar.fragments.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化ViewPager
        viewPager = findViewById(R.id.view_pager)
        bottomNav = findViewById(R.id.bottom_navigation)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // 设置ViewPager的滑动监听器来同步底部导航栏
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 根据当前页面同步更新底部导航栏的选中项
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_calendar
                    1 -> bottomNav.selectedItemId = R.id.nav_settings
                }
            }
        })

        // 设置底部导航栏的点击监听器
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    viewPager.currentItem = 0 // 显示日历页面
                    true
                }
                R.id.nav_settings -> {
                    viewPager.currentItem = 1 // 显示设置页面
                    true
                }
                else -> false
            }
        }
    }


    class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        private val fragments = listOf(
            CalendarFragment(),
            SettingsFragment()
        )

        override fun getItemCount(): Int = fragments.size

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

}

