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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.course.SelectedCourseScreen
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreScreen
import com.lonx.ecjtu.calendar.ui.viewmodel.ScoreViewModel
import com.lonx.ecjtu.calendar.ui.viewmodel.SelectedCourseViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.extra.LocalWindowListPopupState
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.WindowListPopup
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Years

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
            icon = MiuixIcons.Regular.Years
        ),
        NavigationItem(
            label = "成绩",
            icon = ImageVector.vectorResource(R.drawable.ic_score_24dp)
        ),
        NavigationItem(
            label = "已选课程",
            icon = MiuixIcons.Regular.GridView
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
    val selectedCourseViewModel: SelectedCourseViewModel = koinViewModel()

    val scoreScreenState by scoreViewModel.uiState.collectAsStateWithLifecycle()
    val selectedScreenState by selectedCourseViewModel.uiState.collectAsStateWithLifecycle()

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
                                imageVector = MiuixIcons.Regular.Settings,
                                contentDescription = "设置"
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(
                            visible = pagerState.currentPage == 1 || pagerState.currentPage == 2,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(modifier = Modifier.padding(end = 16.dp)) {
                                IconButton(
                                    onClick = {
                                        when (pagerState.currentPage) {
                                            1 -> {
                                                if (scoreScreenState.availableTerms.isNotEmpty()) {
                                                    showTopPopup.value = true
                                                }
                                            }

                                            2 -> {
                                                if (selectedScreenState.availableTerms.isNotEmpty()) {
                                                    showTopPopup.value = true
                                                }
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Regular.Sort,
                                        contentDescription = "学期"
                                    )
                                }
                                WindowListPopup(
                                    show = showTopPopup,
                                    popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                                    alignment = PopupPositionProvider.Align.TopEnd,
                                    onDismissRequest = {
                                        showTopPopup.value = false
                                    }
                                ) {
                                    val state = LocalWindowListPopupState.current
                                    ListPopupColumn {
                                        if (pagerState.currentPage == 1) {
                                            scoreScreenState.availableTerms.forEach { term ->
                                                DropdownImpl(
                                                    text = term,
                                                    optionSize = scoreScreenState.availableTerms.size,
                                                    isSelected = scoreScreenState.currentTerm == term,
                                                    index = scoreScreenState.availableTerms.indexOf(
                                                        term
                                                    ),
                                                    onSelectedIndexChange = {
                                                        state.invoke()
                                                        scoreViewModel.onTermSelected(term)
                                                    }
                                                )
                                            }
                                        } else {
                                            selectedScreenState.availableTerms.forEach { term ->
                                                DropdownImpl(
                                                    text = term,
                                                    optionSize = selectedScreenState.availableTerms.size,
                                                    isSelected = selectedScreenState.currentTerm == term,
                                                    index = selectedScreenState.availableTerms.indexOf(
                                                        term
                                                    ),
                                                    onSelectedIndexChange = {
                                                        state.invoke()
                                                        selectedCourseViewModel.onTermSelected(
                                                            term
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        showRefreshDialog.value = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = MiuixIcons.Regular.Refresh,
                                        contentDescription = "刷新"
                                    )
                                }
                            }
                        }
                    }

                )
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

                    2 -> {
                        SelectedCourseScreen(
                            topAppBarScrollBehavior = topAppBarScrollBehaviorList[it]
                        )
                    }
                }
            }
            SuperDialog(
                modifier = Modifier.padding(bottom = 16.dp),
                show = showRefreshDialog,
                title = "重新获取数据",
                onDismissRequest = {
                    showRefreshDialog.value = false
                },
                content = {
                    Text(
                        text = "是否重新从教务系统获取数据？\n这可能需要一点时间",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
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
                                if (pagerState.currentPage == 2) {
                                    selectedCourseViewModel.loadCourses(refresh = true)
                                } else if (pagerState.currentPage == 1) {
                                    scoreViewModel.loadScores(refresh = true)
                                }
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
