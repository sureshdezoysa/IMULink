package com.daqmobile.imulink.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daqmobile.imulink.R
import com.daqmobile.imulink.ui.screens.HomeScreen
import com.daqmobile.imulink.ui.screens.SettingsScreen
import com.daqmobile.imulink.ui.screens.HelpScreen
import com.daqmobile.imulink.ui.screens.SensorDetailScreen
import com.daqmobile.imulink.ui.screens.OnboardingScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME               = "home"
    const val SETTINGS           = "settings"
    const val HELP               = "help"
    const val HELP_FROM_SETTINGS = "help_from_settings"
    const val SENSOR_INFO        = "sensor_info"
}

@Composable
fun IMULinkApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val streamState by viewModel.streamState.collectAsState()
    val stopFirst    = stringResource(R.string.status_stop_before_settings)
    val scope        = rememberCoroutineScope()

    var showOnboarding by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        showOnboarding = !viewModel.settingsRepository.isOnboardingDone()
    }

    if (showOnboarding == null) return

    if (showOnboarding == true) {
        OnboardingScreen(
            onFinish = {
                scope.launch {
                    viewModel.settingsRepository.setOnboardingDone()
                    showOnboarding = false
                }
            }
        )
        return
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                viewModel            = viewModel,
                onNavigateToSettings = {
                    // Read streamState at click time — not at composition time
                    if (streamState != StreamState.IDLE) {
                        viewModel.showPopup(stopFirst)
                    } else {
                        navController.navigate(Routes.SETTINGS)
                    }
                },
                onNavigateToHelp = { navController.navigate(Routes.HELP) }
            )
        }

        // ── Settings — slides from right ──────────────────────────────────
        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) {
            SettingsScreen(
                viewModel    = viewModel,
                onBack       = { navController.popBackStack() },
                onHelp       = { navController.navigate(Routes.HELP_FROM_SETTINGS) },
                onSensorInfo = { navController.navigate(Routes.SENSOR_INFO) }
            )
        }

        // ── Help from Home — slides from left ─────────────────────────────
        composable(
            route = Routes.HELP,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            }
        ) {
            HelpScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        // ── Help from Settings — slides from right ────────────────────────
        composable(
            route = Routes.HELP_FROM_SETTINGS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) {
            HelpScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        // ── Sensor info — slides from right ───────────────────────────────
        composable(
            route = Routes.SENSOR_INFO,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) {
            SensorDetailScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}
