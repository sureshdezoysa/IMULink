package com.daqmobile.imulink.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daqmobile.imulink.ui.screens.HomeScreen
import com.daqmobile.imulink.ui.screens.SettingsScreen
import com.daqmobile.imulink.ui.screens.HelpScreen

object Routes {
    const val HOME     = "home"
    const val SETTINGS = "settings"
    const val HELP     = "help"
}

@Composable
fun IMULinkApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel            = viewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToHelp     = { navController.navigate(Routes.HELP) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}
