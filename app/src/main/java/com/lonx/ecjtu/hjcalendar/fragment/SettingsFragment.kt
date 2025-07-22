package com.lonx.ecjtu.hjcalendar.fragment

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.BuildConfig
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.appWidget.CourseWidgetProvider
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil
import androidx.core.content.edit
import androidx.core.net.toUri

class SettingsFragment : PreferenceFragmentCompat() {

    private val developerIntent by lazy { Intent(Intent.ACTION_VIEW,
        getString(R.string.developer_url).toUri())}
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW,
        getString(R.string.source_code_url).toUri())}

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
        setVersionInfo()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key = preference.key

        when (key) {
            getString(R.string.developer) -> openURL(developerIntent)
            getString(R.string.source_code_key) -> openURL(sourceCodeIntent)
            getString(R.string.tutorial_weixinid_key) -> showAlertDialog(preference.title, R.string.tutorial_weixinid_message)
            getString(R.string.tutorial_date_key) -> showAlertDialog(preference.title, R.string.tutorial_date_message)
            getString(R.string.weixin_id_key),
            getString(R.string.no_course_key) -> showInputDialog(preference)

            getString(R.string.pin_appwidget_key) -> addAppWidget()

            getString(R.string.check_update_key) -> {
                val isCheckUpdate = prefs.getBoolean(key, true)
                val msg = if (isCheckUpdate) "已开启更新检查" else "已关闭更新检查"
                ToastUtil.showToast(requireContext(), msg)
            }

            getString(R.string.check_update_now_key) -> {
                checkUpdate()
            }
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun addAppWidget() {
        try {
            val context = context ?: return
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, CourseWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
                ToastUtil.showToast(context, "已添加小组件到桌面")
            } else {
                ToastUtil.showToast(context, "您的设备不支持固定桌面小组件")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            context?.let {
                ToastUtil.showToast(it, "添加失败了~")
            }
        }
    }

    private fun showAlertDialog(titleResId: CharSequence?, messageResId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleResId)
            .setMessage(getString(messageResId))
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun checkUpdate() {
        // TODO: 实现更新逻辑，例如跳转到 GitHub Releases 或使用更新库
        ToastUtil.showToast(requireContext(), "暂未实现更新检查")
    }

    private fun showInputDialog(preference: Preference) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edittext, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.text_input)

        val key = when (preference.key) {
            getString(R.string.weixin_id_key) -> "weixin_id"
            getString(R.string.no_course_key) -> "no_course_text"
            else -> null
        }

        if (key == null) return

        val savedValue = prefs.getString(key, "")
        textInput.setText(savedValue)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                var inputText = textInput.text?.toString()?.trim() ?: ""

                if (inputText.startsWith("https://jwxt.ecjtu.edu.cn/weixin")) {
                    ToastUtil.showToast(requireContext(), "已获取链接")
                    inputText = inputText.substringAfter("=")
                }

                prefs.edit { putString(key, inputText) }

                val message = if (key == "weixin_id")
                    "weiXinID已保存，在日历页面下拉刷新课表"
                else
                    "自定义文本已保存"

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
            Snackbar.make(
                requireView(),
                getString(R.string.snackbar_intent_failed),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun setVersionInfo() {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val buildTime = BuildConfig.BUILD_TIME

        findPreference<Preference>(getString(R.string.app_version_key))?.summary =
            "版本: $versionName ($versionCode)\n最后编译时间: $buildTime"
    }
}
