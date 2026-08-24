package com.SkrinVex.syncwave.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.ui.screens.MainScreen
import com.SkrinVex.syncwave.app.ui.screens.auth.AuthViewModel
import com.SkrinVex.syncwave.app.ui.screens.auth.LoginScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val container = SyncWaveApplication.instance.container

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(
                    container.checkAuthStatusUseCase,
                    container.loginUseCase,
                    container.setupAdminUseCase,
                    container.saveServerUrlUseCase,
                    container.getServerUrlUseCase,
                    container.getSavedSessionUseCase
                )
            )
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
