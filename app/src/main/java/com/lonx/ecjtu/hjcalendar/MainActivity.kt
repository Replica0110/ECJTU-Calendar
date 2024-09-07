package com.lonx.ecjtu.hjcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.hjcalendar.fragments.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var calendarFragment: CalendarFragment? = null
    private var settingsFragment: SettingsFragment? = null
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
}
