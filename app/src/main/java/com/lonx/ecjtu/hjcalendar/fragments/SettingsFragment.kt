package com.lonx.ecjtu.hjcalendar.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.lonx.ecjtu.hjcalendar.MainActivity
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.viewModels.CalendarViewModel


class SettingsFragment : PreferenceFragmentCompat() {

    private val githubIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02")) }
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02/ECJTU-Calendar")) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)

        // 获取 EditTextPreference
        val weixinIdPreference: EditTextPreference? = findPreference(getString(R.string.weixin_id_key))

        // 监听输入变化
        weixinIdPreference?.setOnPreferenceChangeListener { _, newValue ->
            val newWeixinId = newValue as String

            // 获取旧数据
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val oldWeixinId = sharedPreferences.getString(getString(R.string.weixin_id_key), "")

            // 判断新旧数据是否一致
            if (newWeixinId != oldWeixinId) {
                // 数据不一致，触发更新
                Log.d(TAG, "New weixinId: $newWeixinId")
                val viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
                viewModel.fetchCourseInfo(newWeixinId)

                // 保存新的 weixinId 到 SharedPreferences
                sharedPreferences.edit().putString(getString(R.string.weixin_id_key), newWeixinId).apply()

                // 通知 MainActivity 数据已变化
                (activity as MainActivity).setDataChanged()
            }

            true
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.developer) -> openURL(githubIntent)
            getString(R.string.source_code_key) -> openURL(sourceCodeIntent)
            else -> {
                if (preference.title == getString(R.string.tutorial_title)) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(preference.title)
                        .setMessage(getString(R.string.tutorial_message))
                        .show()
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
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

