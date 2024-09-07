package com.lonx.ecjtu.hjcalendar.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.R


class SettingsFragment : PreferenceFragmentCompat() {

    private val githubIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02")) }
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02/ECJTU-Calendar")) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
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
                } else if (preference.title == getString(R.string.weixin_id_title)) {
                    // 调用自定义对话框并处理输入
                    weiXinidInputDialog(preference)
                }
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun weiXinidInputDialog(preference: Preference) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edittext, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.text_input)
        val sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val savedValue = sharedPreferences.getString("weixin_id", "")  // 使用固定键
        textInput.setText(savedValue)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                val inputText = textInput.text.toString()

                with(sharedPreferences.edit()) {
                    putString("weixin_id", inputText)
                    apply()
                }

                Toast.makeText(requireContext(), "weiXinID已保存，在日历页面下拉刷新课表", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
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
