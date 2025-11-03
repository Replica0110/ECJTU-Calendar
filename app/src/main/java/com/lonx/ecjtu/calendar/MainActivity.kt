package com.lonx.ecjtu.calendar

import MainViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lonx.ecjtu.calendar.ui.animations.NavigationAnimationTransitions
import com.lonx.ecjtu.calendar.ui.component.UpdateDialog
import com.lonx.ecjtu.calendar.ui.screen.academiccalendar.AcademicCalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingsScreen
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.util.UpdateManager
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.edgeToEdge
import com.moriafly.salt.ui.util.WindowUtil
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String, val label: String, val icon: Int) {
    object Calendar : Screen("calendar", "课表", R.drawable.ic_course_24dp)
    object MyScore : Screen("my_score", "我的成绩", R.drawable.ic_score_24dp)
    object Settings : Screen("settings", "设置", R.drawable.ic_settings_24dp)

    object AcademicCalendar : Screen("academic_calendar", "校历", R.drawable.ic_date_24dp)
}


val LocalNavController = compositionLocalOf<NavController> {
    error("LocalNavController is not provided.")
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by lazy { getViewModel() }

    @OptIn(UnstableSaltUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        edgeToEdge()
        setContent {
            mainViewModel.onStartup()
            mainViewModel.handleIntent(intent)
            CalendarTheme {
                // 状态管理
                val updateManager: UpdateManager = koinInject()
                val updateState by updateManager.state.collectAsState()
                val context = LocalContext.current
                val isDarkTheme = SaltTheme.configs.isDarkTheme

                // 副作用：根据主题设置状态栏颜色
                LaunchedEffect(isDarkTheme) {
                    val barColor = if (isDarkTheme) WindowUtil.BarColor.White else WindowUtil.BarColor.Black
                    WindowUtil.setStatusBarForegroundColor(window, barColor)
                    WindowUtil.setNavigationBarForegroundColor(window, barColor)
                }

                val navController = rememberNavController()
                // 定义顶层页面，用于判断是否显示底部导航栏
                val bottomBarScreens = listOf(Screen.Calendar, Screen.Settings)

                CompositionLocalProvider(LocalNavController provides navController) {
                    Scaffold(
                        bottomBar = {
                            // 判断当前路由是否在顶层页面列表中，如果是，则显示底部导航栏
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val shouldShowBottomBar = bottomBarScreens.any { it.route == currentDestination?.route }

                            if (shouldShowBottomBar) {
                                BottomBar {
                                    bottomBarScreens.forEach { screen ->
                                        BottomBarItem(
                                            state = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    // 弹出到导航图的起始目的地，避免堆栈积累
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    // 避免在栈顶重复创建实例
                                                    launchSingleTop = true
                                                    // 重新选择已选项时，恢复其状态
                                                    restoreState = true
                                                }
                                            },
                                            painter = painterResource(id = screen.icon),
                                            text = screen.label
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Calendar.route,
                            modifier = Modifier.padding(innerPadding),
                            enterTransition = { NavigationAnimationTransitions.enterTransition },
                            exitTransition = { NavigationAnimationTransitions.exitTransition },
                            popEnterTransition = { NavigationAnimationTransitions.popEnterTransition },
                            popExitTransition = { NavigationAnimationTransitions.popExitTransition },
                        ) {
                            composable(route = Screen.Calendar.route) {
                                CalendarScreen()
                            }
                            composable(route = Screen.Settings.route) {
                                SettingsScreen(
                                    onNavigateToAcademicCalendar = { navController.navigate(Screen.AcademicCalendar.route) }
                                )
                            }
                            composable(route = Screen.AcademicCalendar.route) {
                                AcademicCalendarScreen(onNavigateBack = { navController.popBackStack() })
                            }
                        }

                        if (updateState.updateDTO != null) {
                            UpdateDialog(
                                updateState = updateState,
                                onDismiss = { updateManager.dismissUpdateDialog() },
                                onDownload = { updateManager.startDownload(context) },
                                onCancelDownload = { updateManager.cancelDownload() },
                                onInstall = { updateManager.installUpdate(context) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.handleIntent(intent)
    }
}