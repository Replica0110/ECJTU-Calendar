package com.lonx.ecjtu.hjcalendar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.hjcalendar.fragments.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var calendarFragment: CalendarFragment? = null
    private var settingsFragment: SettingsFragment? = null
    private var isDataChanged: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化Fragment
        calendarFragment = CalendarFragment()
        settingsFragment = SettingsFragment()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    // 根据全局变量 isDataChanged 决定如何切换 Fragment
                    if (isDataChanged) {
                        // 销毁并重新创建 CalendarFragment
                        Log.e("MainActivity", "Data changed, recreating CalendarFragment")
                        calendarFragment = CalendarFragment()
                        isDataChanged = false // 重置标志位
                    }
                    switchFragment(calendarFragment!!)
                    true
                }
                R.id.nav_settings -> {
                    switchFragment(settingsFragment!!)
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            // 默认显示CalendarFragment
            switchFragment(calendarFragment!!)
        }
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        // 遍历所有已添加的Fragment，隐藏它们
        fragmentManager.fragments.forEach { fm ->
            if (fm != fragment) {
                transaction.hide(fm)
            }
        }

        // 显示目标Fragment
        if (!fragment.isAdded) {
            transaction.add(R.id.fragment_container, fragment)
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
        return true
    }
    // 提供一个方法供 SettingsFragment 调用，标记数据已更改
    fun setDataChanged() {
        isDataChanged = true
    }
}
