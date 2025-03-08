package com.lonx.ecjtu.hjcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.hjcalendar.fragment.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragment.SettingsFragment
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent(intent)
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
                    0 -> {
                        bottomNav.selectedItemId = R.id.nav_calendar
                        updateActionBarTitle("日历")
                    }
                    1 -> {
                        bottomNav.selectedItemId = R.id.nav_settings
                        updateActionBarTitle("设置")
                    }
                }
            }
        })

        // 设置底部导航栏的点击监听器
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    viewPager.currentItem = 0 // 显示日历页面
                    updateActionBarTitle("日历")
                    true
                }
                R.id.nav_settings -> {
                    viewPager.currentItem = 1 // 显示设置页面
                    updateActionBarTitle("设置")
                    true
                }
                else -> false
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }
    private fun parseIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            uri?.let {
                val weixinID = it.getQueryParameter("weiXinID")
                weixinID?.let { id ->
                    prefs.edit().putString("weixin_id", id).apply()
                }
            }
        }
    }
    private fun updateActionBarTitle(title: String) {
        supportActionBar?.title = title
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
