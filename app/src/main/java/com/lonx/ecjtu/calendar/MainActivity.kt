package com.lonx.ecjtu.calendar

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.rememberNavController
import com.lonx.ecjtu.calendar.ui.component.UpdateBottomSheet
import com.lonx.ecjtu.calendar.ui.theme.CalendarTheme
import com.lonx.ecjtu.calendar.ui.theme.keyColorFor
import com.lonx.ecjtu.calendar.ui.viewmodel.MainViewModel
import com.lonx.ecjtu.calendar.util.Logger
import com.lonx.ecjtu.calendar.util.Logger.Tags
import com.lonx.ecjtu.calendar.util.UpdateManager
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.Scaffold


class MainActivity: ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        viewModel.onStartup()
        viewModel.handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            Logger.d(Tags.APP, "MainActivity uiState: colorMode=${uiState.colorMode}, keyColorIndex=${uiState.keyColorIndex}")
            val darkMode = uiState.colorMode == 2 || uiState.colorMode == 5 ||
                (isSystemInDarkTheme() && (uiState.colorMode == 0 || uiState.colorMode == 3))
            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false // Xiaomi moment, this code must be here
                }

                onDispose {}
            }
            val keyColor = keyColorFor(uiState.keyColorIndex)

            CalendarTheme(
                colorMode = uiState.colorMode,
                keyColor = keyColor
            ) {
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
//                            updateManager.resetUpdateState()
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
