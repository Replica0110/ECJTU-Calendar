package com.lonx.ecjtu.hjcalendar.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.lonx.ecjtu.hjcalendar.R
import com.lonx.ecjtu.hjcalendar.utils.ToastUtil


class SettingsFragment : PreferenceFragmentCompat() {

    private val githubIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02")) }
    private val sourceCodeIntent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Agiens02/ECJTU-Calendar")) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_settings, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val context = requireContext()
        when (preference.key) {
            getString(R.string.developer) -> openURL(githubIntent)
            getString(R.string.source_code_key) -> openURL(sourceCodeIntent)
            getString(R.string.tutorial_weixinid_key) -> showAlertDialog(context, preference.title, R.string.tutorial_weixinid_message)
            getString(R.string.tutorial_date_key) -> showAlertDialog(context, preference.title, R.string.tutorial_date_message)
            getString(R.string.weixin_id_key) -> showInputDialog(preference)
            getString(R.string.no_course_key) -> showInputDialog(preference)
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showAlertDialog(context: Context, titleResId: CharSequence?, messageResId: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleResId)
            .setMessage(getString(messageResId))
            .show()
    }


    private fun showInputDialog(preference: Preference) {
        Log.e("SettingsFragment", "点击了${preference.title}")
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edittext, null)
        val textInput = dialogView.findViewById<TextInputEditText>(R.id.text_input)
        val sharedPreferences = requireActivity().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        if (preference.key == getString(R.string.weixin_id_key)){
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
                    ToastUtil.showToast(requireContext(), "weiXinID已保存，在日历页面下拉刷新课表")
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
    } else if (preference.key == getString(R.string.no_course_key))
    {
        val savedValue = sharedPreferences.getString("no_course_text", "")
        textInput.setText(savedValue)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(preference.title)
            .setView(dialogView)
            .setPositiveButton("保存") { dialog, _ ->
                val inputText = textInput.text.toString()
                with(sharedPreferences.edit()) {
                    putString("no_course_text", inputText)
                    apply()
                }
                ToastUtil.showToast(requireContext(), "自定义文本已保存")
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
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
