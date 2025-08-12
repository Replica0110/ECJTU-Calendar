package com.lonx.ecjtu.hjcalendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtu.hjcalendar.databinding.ActivityMainBinding
import com.lonx.ecjtu.hjcalendar.fragment.CalendarFragment
import com.lonx.ecjtu.hjcalendar.fragment.SettingsFragment
import com.lonx.ecjtu.hjcalendar.logic.DownloadState
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private var updateDialog: AlertDialog? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.handleIntent(intent)
        viewModel.runStartupChecks()

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

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // 观察更新检查的结果
        viewModel.updateResult.observe(this) { result ->
            if (result is UpdateCheckResult.NewVersion) {
                viewModel.newVersionInfo = result.info
                showUpdateDialog(result.info)
            }

        }

        // 观察来自 ViewModel 的 Toast 消息请求
        viewModel.toastMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(this, message)
            }
        }
        viewModel.downloadState.observe(this) { state ->
            if (updateDialog == null || !updateDialog!!.isShowing) return@observe
            val positiveButton = updateDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            when (state) {
                is DownloadState.Idle -> {
                    positiveButton?.text = "立即下载"
                }
                is DownloadState.InProgress -> {
                    positiveButton?.text = "取消下载 (${state.progress}%)"
                }
                is DownloadState.Success -> {
                    ToastUtil.showToast(this, "下载完成，即将安装...")
                    updateDialog?.dismiss()
                    viewModel.updateManager.installApk(this, state.file)
                    viewModel.resetDownloadState()
                }
                is DownloadState.Error -> {
                    ToastUtil.showToast(this, "下载失败: ${state.exception.message}")
                    positiveButton?.text = "重试"
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 当应用已在后台时，通过新 Intent 启动，同样委托给 ViewModel 处理
        viewModel.handleIntent(intent)
    }

    private fun showUpdateDialog(info: UpdateManager.UpdateInfo) {
        updateDialog?.dismiss()

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle("发现新版本: ${info.versionName}")
            .setMessage(info.releaseNotes)
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            // 1. 将 listener 设置为 null，这是阻止自动关闭的第一步
            .setPositiveButton("立即下载", null)
            .setOnDismissListener {
                viewModel.cancelDownload()
                updateDialog = null
            }

        updateDialog = builder.create()

        // 2. 在对话框显示后，手动获取按钮并设置我们自己的监听器
        // 这样做可以完全覆盖默认的“点击后关闭”行为
        updateDialog?.setOnShowListener {
            val positiveButton = updateDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton?.setOnClickListener {
                // 根据当前状态决定是开始下载还是取消下载
                val currentState = viewModel.downloadState.value
                if (currentState is DownloadState.InProgress) {
                    viewModel.cancelDownload()
                } else {
                    viewModel.downloadUpdate()
                }
            }
        }

        updateDialog?.show()
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