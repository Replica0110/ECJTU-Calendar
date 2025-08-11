package com.lonx.ecjtu.hjcalendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtu.hjcalendar.databinding.ActivityMainBinding
import com.lonx.ecjtu.hjcalendar.fragment.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragment.SettingsFragment
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.handleIntent(intent)
        mainViewModel.runStartupChecks()

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // 初始化 ViewPager 和 BottomNav
        binding.viewPager.adapter = ViewPagerAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.bottomNavigation.selectedItemId = R.id.nav_calendar
                        updateActionBarTitle(getString(R.string.calendar_title))
                    }
                    1 -> {
                        binding.bottomNavigation.selectedItemId = R.id.nav_settings
                        updateActionBarTitle(getString(R.string.settings_title))
                    }
                }
            }
        })

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.nav_settings -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                else -> false
            }
        }
        // 初始设置标题
        updateActionBarTitle(getString(R.string.calendar_title))
    }

    private fun setupObservers() {
        // 观察更新检查的结果
        mainViewModel.updateResult.observe(this) { result ->
            if (result is UpdateCheckResult.NewVersion) {
                mainViewModel.newVersionInfo = result.info
                showUpdateDialog(result.info)
            }

        }

        // 观察来自 ViewModel 的 Toast 消息请求
        mainViewModel.toastMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(this, message)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 当应用已在后台时，通过新 Intent 启动，同样委托给 ViewModel 处理
        mainViewModel.handleIntent(intent)
    }

    private fun showUpdateDialog(info: UpdateManager.UpdateInfo) {
        MaterialAlertDialogBuilder(this)
            .setTitle("发现新版本: ${info.versionName}")
            .setMessage(info.releaseNotes)
            .setPositiveButton("立即下载") { _, _ ->
                mainViewModel.downloadUpdate()
            }
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .show()
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

        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}