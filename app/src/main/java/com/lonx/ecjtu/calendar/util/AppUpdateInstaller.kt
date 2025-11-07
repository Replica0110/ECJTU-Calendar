package com.lonx.ecjtu.calendar.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

// 接口，用于依赖注入和测试
interface AppUpdateInstaller {
    fun installApk(context: Context, apkFile: File)
}

// 具体实现
class AppUpdateInstallerImpl : AppUpdateInstaller {
    private val TAG = "AppUpdateInstaller"

    override fun installApk(context: Context, apkFile: File) {
        val authority = "${context.packageName}.provider"
        val apkUri: Uri = try {
            FileProvider.getUriForFile(context, authority, apkFile)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "FileProvider a Uri时出错，请检查provider配置", e)

            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 检查安装未知来源应用的权限（Android 8.0+）
        var hasInstallPermission = false
        try {
            hasInstallPermission = context.packageManager.canRequestPackageInstalls()
        } catch (e: SecurityException) {
            Log.e(TAG, "检查安装权限时发生安全异常，可能未声明REQUEST_INSTALL_PACKAGES权限", e)
            // 直接跳转到设置页面请求权限
            requestInstallPermission(context)
            return
        }

        if (!hasInstallPermission) {
            Log.w(TAG, "没有安装未知应用的权限，正在请求...")
            // 跳转到设置页面请求权限
            requestInstallPermission(context)
            return
        }

        Log.i(TAG, "正在启动系统安装程序...")
        context.startActivity(intent)
    }

    private fun requestInstallPermission(context: Context) {
        val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(settingsIntent)
    }
}