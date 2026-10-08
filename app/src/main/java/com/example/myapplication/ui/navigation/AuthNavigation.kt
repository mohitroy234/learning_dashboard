package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screen.auth.LoginScreen
import com.example.myapplication.ui.screen.auth.RegisterScreen
import com.example.myapplication.ui.viewmodel.auth.AuthViewModel

@Composable
fun AuthNavigation(
    loginState: AuthViewModel.AuthUiState,
    registerState: AuthViewModel.AuthUiState,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    onLoginClick(email, password)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                loginState = loginState
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = onRegisterClick,
                onLoginClick = {
                    navController.popBackStack()
                },
                registerState = registerState
            )
        }
    }
}