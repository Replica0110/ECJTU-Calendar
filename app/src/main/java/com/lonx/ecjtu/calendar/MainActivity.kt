package com.lonx.ecjtu.calendar

import MainViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import com.lonx.ecjtu.calendar.ui.component.UpdateBottomSheet
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.score.ScoreScreen
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingScreen
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.util.UpdateManager
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.extra.SuperBottomSheet


class MainActivity: ComponentActivity() {
    private val viewModel: MainViewModel by lazy { getViewModel() }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        viewModel.onStartup()
        viewModel.handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CalendarTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val updateManager: UpdateManager = koinInject()
                val updateState by updateManager.state.collectAsState()

                Scaffold {
                    DestinationsNavHost(
                        modifier = Modifier,
                        navGraph = NavGraphs.root,
                        navController = navController,
                        defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                {
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    )
                                }

                            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                {
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 5 },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    )
                                }

                            override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                                {
                                    slideInHorizontally(
                                        initialOffsetX = { -it / 5 },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    )
                                }

                            override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                                {
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    )
                                }
                        }
                    )
                    UpdateBottomSheet(
                        updateState = updateState,
                        onDismiss = {
                            updateManager.resetUpdateState()
                        },
                        onDownload = {
                            updateManager.startDownload(context =  context)
                        },
                        onCancelDownload =  {
                            updateManager.cancelDownload()
                        },
                        onInstall = {
                            updateManager.installUpdate(context = context)
                        },
                    )
                }
            }
        }
    }
}
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
        ),
        NavigationItem(
            label = "设置",
            icon = ImageVector.vectorResource(R.drawable.ic_settings_24dp)
        )
    )
    val focusManager = LocalFocusManager.current
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
                    2 -> SettingScreen(
                        topAppBarScrollBehavior = topAppBarScrollBehaviorList[it],
                        navigator = navigator,
                        focusManager = focusManager
                    )
                }
            }
        }
    }

}
