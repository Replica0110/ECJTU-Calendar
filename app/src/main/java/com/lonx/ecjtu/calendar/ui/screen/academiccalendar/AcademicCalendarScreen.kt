package com.lonx.ecjtu.calendar.ui.screen.academiccalendar

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.lonx.ecjtu.calendar.ui.component.MessageCard
import com.lonx.ecjtu.calendar.ui.component.MessageType
import com.lonx.ecjtu.calendar.ui.viewmodel.AcademicCalendarViewModel
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
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.icons.useful.ImmersionMore
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.getWindowSize
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.net.URL

@Composable
@Destination<RootGraph>()
fun AcademicCalendarScreen(
    navigator: DestinationsNavigator
) {
    val viewModel: AcademicCalendarViewModel = koinInject()

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showTopPopup = remember { mutableStateOf(false) }
    val windowSize = getWindowSize()


    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            SmallTopAppBar(
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
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {
                            showTopPopup.value = true
                        }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Useful.ImmersionMore,
                            contentDescription = "更多",
                            tint = colorScheme.onBackground
                        )
                    }
                    ListPopup(
                        show = showTopPopup,
                        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                        alignment = PopupPositionProvider.Align.TopRight,
                        onDismissRequest = {
                            showTopPopup.value = false
                        },
                        enableWindowDim = false
                    ) {
                        ListPopupColumn {
                            DropdownImpl(
                                text = "保存图片",
                                optionSize = 3,
                                isSelected = false,
                                onSelectedIndexChange = {
                                    scope.launch {
                                        downloadImage(context, uiState.imageUrl, uiState.imageData, scope)
                                    }
                                    showTopPopup.value = false
                                },
                                index = 0
                            )
                            DropdownImpl(
                                text = "在浏览器打开",
                                optionSize = 3,
                                isSelected = false,
                                onSelectedIndexChange = {
                                    uiState.imageUrl?.let { url ->
                                        scope.launch {
                                            openUrl(context, url, scope)
                                        }
                                    }
                                    showTopPopup.value = false
                                },
                                index = 1
                            )
                            DropdownImpl(
                                text = "刷新",
                                optionSize = 3,
                                isSelected = false,
                                onSelectedIndexChange = {
                                    viewModel.refresh()
                                    showTopPopup.value = false
                                },
                                index = 2
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .scrollEndHaptic()
                .overScrollVertical()
                .padding(contentPadding)
                .height(windowSize.height.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        MessageCard(
                            message = uiState.error.orEmpty(),
                            type = MessageType.Error,
                            onClick = {
                                viewModel.refresh()
                            }
                        )
                    }
                }

                uiState.imageUrl != null -> {
                    val zoomableState = rememberZoomableState()

                    val imageState = rememberZoomableImageState(zoomableState)
                    ZoomableAsyncImage(
                        model = uiState.imageUrl,
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

private fun downloadImage(
    context: Context,
    imageUrl: String?,
    imageData: ByteArray?,
    scope: CoroutineScope
) {
    scope.launch {
        try {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "开始保存...", Toast.LENGTH_SHORT).show()
            }

            val bytes = imageData ?: imageUrl?.let {
                withContext(Dispatchers.IO) {
                    URL(it).openStream().use { stream -> stream.readBytes() }
                }
            }

            bytes?.let {
                withContext(Dispatchers.IO) {
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
                        uri?.let { imageUri ->
                            context.contentResolver.openOutputStream(imageUri)?.use { output ->
                                output.write(it)
                            }
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AcademicCalendar", "保存图片失败", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

private fun openUrl(context: Context, url: String, scope: CoroutineScope) {
    scope.launch {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AcademicCalendar", "打开链接失败", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "打开链接失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}