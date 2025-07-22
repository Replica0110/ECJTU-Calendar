package com.lonx.ecjtu.hjcalendar.utils

import android.content.Context
import android.widget.Toast

object ToastUtil {
    private var currentToast: Toast? = null

    /**
     * 显示 Toast，取消之前的 Toast
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        // 如果有已经存在的 Toast，取消它
        currentToast?.cancel()

        // 创建并显示新的 Toast
        currentToast = Toast.makeText(context, message, duration)
        currentToast?.show()
    }
}
