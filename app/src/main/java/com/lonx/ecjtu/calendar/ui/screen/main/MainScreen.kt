package com.lonx.ecjtu.calendar.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.lonx.ecjtu.calendar.R
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Settings

val LocalPagerState = compositionLocalOf<PagerState> { error("No pager state") }
val LocalHandlePageChange = compositionLocalOf<(Int) -> Unit> { error("No handle page change") }
@Composable
@Destination<RootGraph>(start = true)
fun MainScreen(
    navigator: DestinationsNavigator
){
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

    val handlePageChange: (Int) -> Unit = remember(pagerState, coroutineScope) {
        { page ->
            coroutineScope.launch { pagerState.animateScrollToPage(page) }
        }
    }
    val topAppBarScrollBehaviorList = List(routes.size) { MiuixScrollBehavior() }
    val currentScrollBehavior = topAppBarScrollBehaviorList[pagerState.currentPage]

    CompositionLocalProvider(
        LocalPagerState provides pagerState,
        LocalHandlePageChange provides handlePageChange
    ){
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
                                    navigator.navigate(SettingScreenDestination())
                                },
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Useful.Settings,
                                    contentDescription = "设置"
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                val pagerState = LocalPagerState.current
                val handlePageChange = LocalHandlePageChange.current
                NavigationBar(
                    items = routes,
                    selected =  pagerState.currentPage,
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
        }
    }

}
