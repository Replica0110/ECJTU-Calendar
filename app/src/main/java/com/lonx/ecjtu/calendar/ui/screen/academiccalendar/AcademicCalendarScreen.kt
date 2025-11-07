package com.lonx.ecjtu.calendar.ui.screen.academiccalendar

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.data.network.Constants
import com.lonx.ecjtu.calendar.domain.usecase.calendar.GetAcademicCalendarUseCase
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.icons.useful.Save
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.io.IOException
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>()
fun AcademicCalendarScreen(
    navigator: DestinationsNavigator
) {
    val getAcademicCalendarUseCase: GetAcademicCalendarUseCase = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scrollBehavior = MiuixScrollBehavior()
    val showSaveDialog = remember { mutableStateOf(false) }
    val windowSize = getWindowSize()
    LaunchedEffect(Unit) {
        getAcademicCalendarUseCase(url = Constants.ACADEMIC_CALENDAR_URL).fold(
            onSuccess = { url ->
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
    // 设置topbar默认收起
    LaunchedEffect(scrollBehavior.state.heightOffsetLimit) {
        val limit = scrollBehavior.state.heightOffsetLimit
        if (limit != -Float.MAX_VALUE && scrollBehavior.state.heightOffset == 0f) {
            scrollBehavior.state.heightOffset = limit
        }
    }

    SuperDialog(
        modifier = Modifier.padding(bottom = 16.dp),
        show = showSaveDialog,
        title = "保存到相册",
        onDismissRequest = {
            showSaveDialog.value = false
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                text = "取消",
                onClick = {
                    showSaveDialog.value = false
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = "确定",
                onClick = {
                    imageUrl?.let { url ->
                        scope.launch {
                            downloadImage(context, url, scope)
                        }
                    }
                    showSaveDialog.value = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary()
            )
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "教学校历",
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = {
                            navigator.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Basic.ArrowRight,
                            contentDescription = "返回",
                            tint = colorScheme.onBackground,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (imageUrl != null) {
                FloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        showSaveDialog.value = true
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Useful.Save,
                        contentDescription = "保存到相册",
                        tint = colorScheme.onBackground
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .padding(contentPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .height(windowSize.height.dp)
        ) {
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MessageCard(
                            message = error.orEmpty(),
                            type = MessageType.Info,
                            onClick = {

                            }
                        )
                    }
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

                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
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
                        Toast.makeText(
                            context,
                            "请在设置中授予存储权限以保存图片",
                            Toast.LENGTH_LONG
                        ).show()
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