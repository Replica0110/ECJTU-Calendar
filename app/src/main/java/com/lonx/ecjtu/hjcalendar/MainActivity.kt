package com.lonx.ecjtu.hjcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lonx.ecjtu.hjcalendar.fragments.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var calendarFragment: CalendarFragment? = null
    private var settingsFragment: SettingsFragment? = null
    private var currentFragment: Fragment? = null

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
        } else {
            // 恢复之前的Fragment状态
            savedInstanceState.getParcelable<Fragment.SavedState>("calendarFragmentState")?.let {
                calendarFragment!!.setInitialSavedState(it)
            }
            savedInstanceState.getParcelable<Fragment.SavedState>("settingsFragmentState")?.let {
                settingsFragment!!.setInitialSavedState(it)
            }
            savedInstanceState.getString("currentFragmentTag")?.let { tag ->
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    switchFragment(fragment)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存Fragment状态
        calendarFragment?.let {
            val calendarState = supportFragmentManager.saveFragmentInstanceState(it)
            outState.putParcelable("calendarFragmentState", calendarState)
        }
        settingsFragment?.let {
            val settingsState = supportFragmentManager.saveFragmentInstanceState(it)
            outState.putParcelable("settingsFragmentState", settingsState)
        }
        currentFragment?.let {
            outState.putString("currentFragmentTag", it.tag)
        }
    }


    private fun switchFragment(fragment: Fragment): Boolean {
        if (fragment === currentFragment) {
            return false
        }

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        currentFragment?.let { transaction.hide(it) }

        if (!fragment.isAdded) {
            transaction.add(R.id.fragment_container, fragment, fragment::class.java.simpleName)
        } else {
            transaction.show(fragment)
        }

        transaction.commit()
        currentFragment = fragment
        return true
    }
}
