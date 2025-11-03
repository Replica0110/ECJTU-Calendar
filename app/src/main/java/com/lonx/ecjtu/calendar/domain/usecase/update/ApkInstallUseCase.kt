package com.lonx.ecjtu.calendar.domain.usecase.update

import android.content.Context
import com.lonx.ecjtu.calendar.ui.util.AppUpdateInstaller
import java.io.File

class ApkInstallUseCase(
    private val appUpdateInstaller: AppUpdateInstaller
) {
    operator fun invoke(context: Context, apkFile: File) {
        appUpdateInstaller.installApk(context, apkFile)
    }
}