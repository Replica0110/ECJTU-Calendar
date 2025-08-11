package com.lonx.ecjtu.hjcalendar.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.logic.UpdateCheckResult
import com.lonx.ecjtu.hjcalendar.logic.UpdateManager
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import com.lonx.ecjtu.hjcalendar.viewmodel.SettingsViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

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
    private fun setupObservers() {
        // 观察更新检查的结果
        viewModel.updateResult.observe(this) { result ->
            when (result) {
                is UpdateCheckResult.NewVersion -> {
                    viewModel.newVersionInfo = result.info
                    showUpdateDialog(result.info)
                }
                is UpdateCheckResult.NoUpdateAvailable -> ToastUtil.showToast(requireContext(), "已经是最新版本")
                is UpdateCheckResult.ApiError -> ToastUtil.showToast(requireContext(), "检查更新失败: API 错误 ${result.code}")
                is UpdateCheckResult.NetworkError -> ToastUtil.showToast(requireContext(), "检查更新失败: 请检查网络连接")
                is UpdateCheckResult.ParsingError -> ToastUtil.showToast(requireContext(), "检查更新失败: 无法解析服务器响应")
                is UpdateCheckResult.VersionError -> ToastUtil.showToast(requireContext(), "检查更新失败: 应用内部错误")
            }
        }

        viewModel.pinWidgetResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                ToastUtil.showToast(requireContext(), message)
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("发现新版本: ${info.versionName}")
            .setMessage(info.releaseNotes)
            .setPositiveButton("立即下载") { _, _ -> viewModel.downloadUpdate() }
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .show()
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