package com.lonx.ecjtu.hjcalendar.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.lonx.ecjtu.hjcalendar.BuildConfig
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil

class SettingsFragment : PreferenceFragmentCompat() {

    private val developerIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_url)))}
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_url)))}

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
        setVersionInfo()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.developer) -> openURL(developerIntent)
            getString(R.string.source_code_key) -> openURL(sourceCodeIntent)
            getString(R.string.tutorial_weixinid_key) -> showAlertDialog(preference.title, R.string.tutorial_weixinid_message)
            getString(R.string.tutorial_date_key) -> showAlertDialog(preference.title, R.string.tutorial_date_message)
            getString(R.string.weixin_id_key), getString(R.string.no_course_key) -> showInputDialog(preference)
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showAlertDialog(titleResId: CharSequence?, messageResId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleResId)
            .setMessage(getString(messageResId))
            .show()
    }

    private fun showInputDialog(preference: Preference) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edittext, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.text_input)
        val sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val key = when (preference.key) {
            getString(R.string.weixin_id_key) -> "weixin_id"
            getString(R.string.no_course_key) -> "no_course_text"
            else -> ""
        }
        val savedValue = sharedPreferences.getString(key, "")
        textInput.setText(savedValue)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                val inputText = textInput.text.toString()
                with(sharedPreferences.edit()) {
                    putString(key, inputText)
                    apply()
                }
                val message = if (key == "weixin_id") "weiXinID已保存，在日历页面下拉刷新课表" else "自定义文本已保存"
                ToastUtil.showToast(requireContext(), message)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
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
    private fun setVersionInfo() {
        // 获取应用版本号、版本名和编译时间
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildTime = BuildConfig.BUILD_TIME

        // 查找用于显示版本信息的Preference
        val versionPreference: Preference? = findPreference(getString(R.string.app_version_key))

        // 设置版本信息到summary中
        versionPreference?.summary = "版本: $versionName ($versionCode)\n最后编译时间: $buildTime"
    }
}
