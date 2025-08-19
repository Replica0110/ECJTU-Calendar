package com.lonx.ecjtu.hjcalendar.fragment

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.logic.DownloadState
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.SettingsViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private var updateDialog: AlertDialog? = null
    private val developerIntent by lazy { Intent(Intent.ACTION_VIEW, getString(R.string.developer_url).toUri()) }
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW, getString(R.string.source_code_url).toUri()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)

        setupInitialState()
        setupListeners()
        setupObservers()
    }
    private fun setupInitialState() {
        // 从 ViewModel 获取版本信息
        findPreference<Preference>(getString(R.string.app_version_key))?.summary = viewModel.getVersionSummary()
    }
    private fun setupListeners() {
        val checkUpdatePref = findPreference<SwitchPreference>(getString(R.string.check_update_key))
        checkUpdatePref?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue is Boolean) {
                viewModel.setUpdateCheck(newValue)
            }
            true
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // 观察更新检查的结果
        viewModel.updateResult.observe(this) { result ->
            when (result) {
                is UpdateCheckResult.NewVersion -> {
                    viewModel.newVersionInfo = result.info
                    showUpdateDialog(result.info)
                }
                is UpdateCheckResult.TimeoutError -> ToastUtil.showToast(requireContext(), "检查更新失败: 网络连接超时")
                is UpdateCheckResult.NoUpdateAvailable -> ToastUtil.showToast(requireContext(), "已经是最新版本")
                is UpdateCheckResult.ApiError -> ToastUtil.showToast(requireContext(), "检查更新失败: API 错误 ${result.code}")
                is UpdateCheckResult.NetworkError -> ToastUtil.showToast(requireContext(), "检查更新失败: ${result.exception.message}")
                is UpdateCheckResult.ParsingError -> ToastUtil.showToast(requireContext(), "检查更新失败: 无法解析服务器响应")
            }
        }

        viewModel.pinWidgetResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(requireContext(), message)
            }
        }
        viewModel.downloadState.observe(this) { state ->
            // 如果对话框不存在，则不执行任何操作
            if (updateDialog == null || !updateDialog!!.isShowing) return@observe

            val positiveButton = updateDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            // 获取对“稍后”按钮的引用
            val negativeButton = updateDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)

            when (state) {
                is DownloadState.Idle -> {
                    positiveButton?.text = "立即下载"
                    positiveButton?.setOnClickListener { viewModel.downloadUpdate() }
                    negativeButton?.visibility = View.VISIBLE
                }
                is DownloadState.InProgress -> {
                    positiveButton?.text = "取消下载 (${state.progress}%)"
                    positiveButton?.setOnClickListener { viewModel.cancelDownload() }
                    negativeButton?.visibility = View.GONE
                }
                is DownloadState.Success -> {
                    ToastUtil.showToast(requireContext(), "下载完成，即将安装...")
                    updateDialog?.dismiss()
                    // 调用 UpdateManager 中的安装方法
                    viewModel.updateManager.installApk(requireContext(), state.file)
                    viewModel.resetDownloadState() // 重置状态
                }
                is DownloadState.Error -> {
                    ToastUtil.showToast(requireContext(), "下载失败: ${state.exception.message}")
                    positiveButton?.text = "重试"
                    positiveButton?.setOnClickListener { viewModel.downloadUpdate() }
                    // 在出错后，让用户可以选择“重试”或“稍后”，所以恢复“稍后”按钮的可见性
                    negativeButton?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.developer) -> openURL(developerIntent)
            getString(R.string.source_code_key) -> openURL(sourceCodeIntent)
            getString(R.string.tutorial_weixinid_key) -> showAlertDialog(preference.title, R.string.tutorial_weixinid_message)
            getString(R.string.tutorial_date_key) -> showAlertDialog(preference.title, R.string.tutorial_date_message)
            getString(R.string.weixin_id_key),
            getString(R.string.no_course_key) -> showInputDialog(preference)
            getString(R.string.pin_appwidget_key) -> viewModel.pinWidget()
            getString(R.string.check_update_now_key) -> {
                ToastUtil.showToast(requireContext(), "正在检查更新...")
                viewModel.checkForUpdate()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }


    private fun showUpdateDialog(info: UpdateManager.UpdateInfo) {
        updateDialog?.dismiss()

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle("发现新版本: ${info.versionName}")
            .setMessage(info.releaseNotes)
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .setPositiveButton("立即下载", null)
            .setOnDismissListener {
                viewModel.cancelDownload()
                updateDialog = null
            }

        updateDialog = builder.create()

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

    private fun showAlertDialog(titleResId: CharSequence?, messageResId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleResId)
            .setMessage(getString(messageResId))
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun showInputDialog(preference: Preference) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edittext, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.text_input)
        val key = preference.key ?: return

        val savedValue = when (key) {
            getString(R.string.weixin_id_key) -> viewModel.getWeiXinId()
            getString(R.string.no_course_key) -> viewModel.getNoCourseText()
            else -> ""
        }
        textInput.setText(savedValue)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                val inputText = textInput.text?.toString()?.trim() ?: ""
                viewModel.saveStringPreference(key, inputText)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openURL(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(requireView(), getString(R.string.snackbar_intent_failed), Snackbar.LENGTH_SHORT).show()
        }
    }
}