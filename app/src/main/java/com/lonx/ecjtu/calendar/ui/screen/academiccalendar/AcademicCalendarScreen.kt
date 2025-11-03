package com.lonx.ecjtu.calendar.ui.screen.academiccalendar

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.domain.usecase.calendar.GetAcademicCalendarUseCase
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.koin.compose.koinInject
import java.io.IOException
import java.net.URL

@OptIn(UnstableSaltUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarScreen(
    onNavigateBack: () -> Unit
) {
    val getAcademicCalendarUseCase: GetAcademicCalendarUseCase = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        Log.d("AcademicCalendar", "开始获取校历图片URL")
        getAcademicCalendarUseCase().fold(
            onSuccess = { url ->
                Log.d("AcademicCalendar", "获取校历图片URL成功: $url")
                imageUrl = url
                loading = false
            },
            onFailure = { throwable ->
                val errorMsg = throwable.message ?: "未知错误"
                Log.e("AcademicCalendar", "获取校历图片URL失败: $errorMsg", throwable)
                error = errorMsg
                loading = false
                Toast.makeText(context, "加载校历失败: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
    ) {
        Column {
            TitleBar(
                text = "校历",
                onBack = {
                    onNavigateBack()
                },
                showBackBtn = true
            )
            when {
                loading -> {
                    CircularProgressIndicator()
                }

                error != null -> {
                    Text(
                        text = "加载失败: $error",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }

                imageUrl != null -> {
                    val zoomableState = rememberZoomableState()

                    val imageState = rememberZoomableImageState(zoomableState)
                    ZoomableAsyncImage(
                        model = imageUrl,
                        contentDescription = "校历图片",
                        state = imageState,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            imageUrl?.let { url ->
                                scope.launch {
                                    downloadImage(context, url, scope)
                                }
                            }
                        }
                    )

                }
            }
        }
    }
}

private fun downloadImage(context: Context, imageUrl: String, scope: CoroutineScope) {
    scope.launch {
        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "开始下载...", Toast.LENGTH_SHORT).show()
            }

            withContext(Dispatchers.IO) {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                val inputStream = connection.getInputStream()
                val fileName = "academic_calendar_${System.currentTimeMillis()}.png"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                    }

                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        context.contentResolver.openOutputStream(it).use { outputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "请在设置中授予存储权限以保存图片", Toast.LENGTH_LONG).show()
                    }
                }
                inputStream.close()
            }
        } catch (e: IOException) {
            Log.e("AcademicCalendar", "下载图片失败", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("AcademicCalendar", "下载图片失败", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "下载失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}