package com.daqmobile.imulink.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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

object Routes {
    const val HOME               = "home"
    const val SETTINGS           = "settings"
    const val HELP               = "help"
    const val HELP_FROM_SETTINGS = "help_from_settings"
    const val SENSOR_DETAIL      = "sensor_detail/{sensorKey}"

    fun sensorDetail(key: String) = "sensor_detail/$key"
}

@Composable
fun IMULinkApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val streamState by viewModel.streamState.collectAsState()
    val isStreaming  = streamState != StreamState.IDLE
    val stopFirst    = stringResource(R.string.status_stop_before_settings)

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                viewModel            = viewModel,
                onNavigateToSettings = {
                    if (!isStreaming) navController.navigate(Routes.SETTINGS)
                    else viewModel.showPopup(stopFirst)
                },
                onNavigateToHelp = { navController.navigate(Routes.HELP) }
            )
        }

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
                viewModel      = viewModel,
                onBack         = { navController.popBackStack() },
                onHelp         = { navController.navigate(Routes.HELP_FROM_SETTINGS) },
                onSensorDetail = { key -> navController.navigate(Routes.sensorDetail(key)) }
            )
        }

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

        composable(
            route = Routes.SENSOR_DETAIL,
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
        ) { backStackEntry ->
            val sensorKey = backStackEntry.arguments?.getString("sensorKey") ?: "Accelerometer"
            SensorDetailScreen(
                sensorKey = sensorKey,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}
