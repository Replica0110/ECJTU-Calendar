package com.lonx.ecjtu.calendar

import MainViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.lonx.ecjtu.calendar.ui.animations.NavigationAnimationTransitions
import com.lonx.ecjtu.calendar.ui.component.UpdateDialog
import com.lonx.ecjtu.calendar.ui.screen.calendar.CalendarScreen
import com.lonx.ecjtu.calendar.ui.screen.settings.SettingsScreen
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.util.UpdateManager
import org.koin.android.ext.android.inject
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.edgeToEdge
import com.moriafly.salt.ui.ext.safeMainPadding
import com.moriafly.salt.ui.util.WindowUtil
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.compose.koinInject
import kotlin.getValue

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
                val updateManager: UpdateManager = koinInject()
                val updateState by updateManager.state.collectAsState()
                val context = LocalContext.current
                val isDarkTheme = SaltTheme.configs.isDarkTheme
                LaunchedEffect(isDarkTheme) {
                    if (isDarkTheme) {
                        WindowUtil.setStatusBarForegroundColor(window, WindowUtil.BarColor.White)
                        WindowUtil.setNavigationBarForegroundColor(
                            window,
                            WindowUtil.BarColor.White
                        )
                    } else {
                        WindowUtil.setStatusBarForegroundColor(window, WindowUtil.BarColor.Black)
                        WindowUtil.setNavigationBarForegroundColor(
                            window,
                            WindowUtil.BarColor.Black
                        )
                    }
                }
                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SaltTheme.colors.background)
                        .safeMainPadding(),
                ) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(
                        LocalNavController provides navController
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "calendar",
                            enterTransition = { NavigationAnimationTransitions.enterTransition },
                            exitTransition = { NavigationAnimationTransitions.exitTransition },
                            popEnterTransition = { NavigationAnimationTransitions.popEnterTransition },
                            popExitTransition = { NavigationAnimationTransitions.popExitTransition },
                        ) {
                            composable(
                                route = "calendar"
                            ) {
                                CalendarScreen(
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }
                            composable(
                                route = "settings"
                            ) {
                                SettingsScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        if (updateState.updateInfo != null) {
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