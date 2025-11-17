package com.lonx.ecjtu.calendar.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreScreen
import com.lonx.ecjtu.calendar.ui.viewmodel.ScoreViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Order
import top.yukonga.miuix.kmp.icon.icons.useful.Refresh
import top.yukonga.miuix.kmp.icon.icons.useful.Settings

val LocalPagerState = compositionLocalOf<PagerState> { error("No pager state") }
val LocalHandlePageChange = compositionLocalOf<(Int) -> Unit> { error("No handle page change") }

@Composable
@Destination<RootGraph>(start = true)
fun MainScreen(
    navigator: DestinationsNavigator
) {
    val routes = listOf(
        NavigationItem(
            label = "日历",
            icon = ImageVector.vectorResource(R.drawable.ic_course_24dp)
        ),
        NavigationItem(
            label = "成绩",
            icon = ImageVector.vectorResource(R.drawable.ic_score_24dp)
        )
    )
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { routes.size })

    val showRefreshDialog = remember { mutableStateOf(false) }

    val handlePageChange: (Int) -> Unit = remember(pagerState, coroutineScope) {
        { page ->
            coroutineScope.launch { pagerState.animateScrollToPage(page) }
        }
    }
    val topAppBarScrollBehaviorList = List(routes.size) { MiuixScrollBehavior() }
    val currentScrollBehavior = topAppBarScrollBehaviorList[pagerState.currentPage]

    val scoreViewModel: ScoreViewModel = koinViewModel()

    val scoreScreenState by scoreViewModel.uiState.collectAsStateWithLifecycle()

    val showTopPopup = remember { mutableStateOf(false) }
    CompositionLocalProvider(
        LocalPagerState provides pagerState,
        LocalHandlePageChange provides handlePageChange
    ) {
        val pagerState = LocalPagerState.current
        val handlePageChange = LocalHandlePageChange.current
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    TopAppBar(
                        title = routes[pagerState.currentPage].label,
                        scrollBehavior = currentScrollBehavior,
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navigator.navigate(SettingsScreenDestination())
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Useful.Settings,
                                    contentDescription = "设置"
                                )
                            }
                        },
                        actions = {
                            AnimatedVisibility(
                                visible = pagerState.currentPage == 1,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Row(modifier = Modifier.padding(end = 16.dp)) {
                                    IconButton(
                                        onClick = {
                                            showTopPopup.value = true
                                        },
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Useful.Order,
                                            contentDescription = "学期"
                                        )
                                    }
                                    ListPopup(
                                        show = showTopPopup,
                                        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                                        alignment = PopupPositionProvider.Align.BottomRight,
                                        onDismissRequest = {
                                            showTopPopup.value = false
                                        },
                                        enableWindowDim = false
                                    ) {
                                        ListPopupColumn {
                                            scoreScreenState.availableTerms.forEach { term ->
                                                DropdownImpl(
                                                    text = term,
                                                    optionSize = scoreScreenState.availableTerms.size,
                                                    isSelected = scoreScreenState.currentTerm == term,
                                                    index = scoreScreenState.availableTerms.indexOf(
                                                        term
                                                    ),
                                                    onSelectedIndexChange = {
                                                        showTopPopup.value = false
                                                        scoreViewModel.onTermSelected(term)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            showRefreshDialog.value = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Useful.Refresh,
                                            contentDescription = "刷新"
                                        )
                                    }
                                }
                            }
                        }

                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    items = routes,
                    selected = pagerState.currentPage,
                    onClick = { index -> handlePageChange.invoke(index) }
                )
            }
        ) { paddingValues ->
            HorizontalPager(
                modifier = Modifier.padding(paddingValues),
                state = LocalPagerState.current,
                userScrollEnabled = true,
                overscrollEffect = null
            ) {
                when (it) {
                    0 -> CalendarScreen(
                        topAppBarScrollBehavior = topAppBarScrollBehaviorList[it]
                    )

                    1 -> ScoreScreen(
                        topAppBarScrollBehavior = topAppBarScrollBehaviorList[it]
                    )
                }
            }
            SuperDialog(
                modifier = Modifier.padding(bottom = 16.dp),
                show = showRefreshDialog,
                title = "刷新数据",
                onDismissRequest = {
                    showRefreshDialog.value = false
                },
                content = {
                    Text(
                        text = "是否重新从教务系统获取成绩数据？\n这可能需要一点时间",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            text = "取消",
                            onClick = {
                                showRefreshDialog.value = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            text = "确定",
                            onClick = {
                                scoreViewModel.loadScores(refresh = true)
                                showRefreshDialog.value = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                }
            )
        }
    }

}
